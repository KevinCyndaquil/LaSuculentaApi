CREATE OR REPLACE FUNCTION assign_order(order_id uuid, dish int, kitchener_id uuid)
	RETURNS boolean AS
$$
BEGIN
	IF NOT exists(SELECT 1 FROM kitcheners WHERE id = kitchener_id)
	THEN
		RETURN FALSE;
	END IF;

	PERFORM *
	FROM order_details od
	WHERE od.order_id = assign_order.order_id
	  AND cns = dish
	  AND current_process = 'WAITING_KITCHENER'
		FOR UPDATE;

	UPDATE order_details od
	SET made_by_id      = kitchener_id,
	    current_process = 'GETTING_READY'
	WHERE od.order_id = assign_order.order_id
	  AND cns = dish
	  AND current_process = 'WAITING_KITCHENER';

	RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION finish_order(order_id uuid, dish int, kitchener_id uuid)
	RETURNS boolean AS
$$
BEGIN
	IF NOT exists(SELECT 1 FROM kitcheners WHERE id = kitchener_id)
	THEN
		RETURN FALSE;
	END IF;

	UPDATE order_details od
	SET current_process = 'READY_TO_DELIVER',
	    ready_on        = now()
	WHERE od.order_id = finish_order.order_id
	  AND cns = dish
	  AND made_by_id IS NOT NULL
	  AND current_process = 'GETTING_READY';

	RETURN FOUND;
END;
$$
	LANGUAGE plpgsql;

SELECT count(*) = sum(
	CASE
		WHEN od.current_process = 'READY_TO_DELIVER'
			THEN 1
			ELSE 0 END) AS is_ready
FROM order_details od
WHERE od.order_id = 'd8256706-87e2-433c-884d-a4a81055bc3b';

CREATE OR REPLACE FUNCTION is_ready(order_id uuid)
	RETURNS boolean AS
$$
BEGIN
	RETURN (SELECT count(*) = sum(
		CASE
			WHEN od.current_process = 'READY_TO_DELIVER'
				THEN 1
				ELSE 0 END) AS is_ready
	        FROM order_details od
	        WHERE od.order_id = is_ready.order_id);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION deliver_order(order_id uuid, dish int)
	RETURNS boolean AS
$$
BEGIN
	UPDATE order_details od
	SET current_process = 'FINISHED'
	WHERE od.order_id = deliver_order.order_id
	  AND cns = dish
	  AND made_by_id IS NOT NULL
	  AND current_process = 'READY_TO_DELIVER';

	RETURN FOUND;
END;
$$
	LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION discount_stock()
	RETURNS trigger AS
$$
DECLARE
	rec record;
BEGIN
	FOR rec IN
		SELECT ingredient_id, quantity
		FROM recipes
		WHERE dish_id = new.dish_id
		LOOP
			UPDATE ingredients
			SET stock = stock - rec.quantity
			WHERE id = rec.ingredient_id;

			RAISE NOTICE 'ingredient updated: % and stock %s', rec.ingredient_id, rec.quantity;

			IF NOT FOUND
			THEN
				RAISE EXCEPTION 'Could not discount stock from dish id %', new.dish_id;
			END IF;

		END LOOP;

	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_discount_stock
	AFTER INSERT
	ON order_details
	FOR EACH ROW
EXECUTE FUNCTION discount_stock();

CREATE OR REPLACE FUNCTION sell_dishes_table()
	RETURNS table (
		id         uuid,
		name       text,
		category   text,
		sell_price numeric(10, 2),
		real_cost  numeric(10, 2),
		revenue    numeric(10, 2)
	)
AS
$$
BEGIN
	RETURN QUERY
		SELECT *, (sell_table.sell_price - sell_table.real_cost)::numeric(10, 2) AS revenue
		FROM (SELECT d.id::uuid,
		             d.name::text,
		             d.category::text,
		             d.sell_price::numeric(10, 2),
		             sum(r.quantity * i.cost)::numeric(10, 2) AS real_cost
		      FROM dishes d
			           INNER JOIN recipes r ON d.id = r.dish_id
			           INNER JOIN ingredients i ON r.ingredient_id = i.id
		      GROUP BY d.id, d.name, d.category, d.sell_price) AS sell_table;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM sell_dishes_table();

CREATE OR REPLACE FUNCTION in_process_orders()
	RETURNS setof orders AS
$$
BEGIN
	RETURN QUERY
		SELECT o.*
		FROM orders o
			     INNER JOIN order_details od ON o.id = od.order_id
		WHERE od.current_process != 'FINISHED'
		ORDER BY o.requested_on DESC;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM in_process_orders();

CREATE OR REPLACE FUNCTION sold_orders(since date, until date)
	RETURNS setof orders AS
$$
BEGIN
	RETURN QUERY
		SELECT o.*
		FROM orders o
			     INNER JOIN order_details od ON o.id = od.order_id
		WHERE o.requested_on >= since
		  AND o.requested_on <= until
		  AND od.current_process = 'FINISHED'
		ORDER BY o.requested_on DESC;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM dishes;
UPDATE dishes
SET icon = 'https://lomaculinaria.com/wp-content/uploads/2022/09/Co%CC%81mo-Hacer-un-Omelette-Loma-Culinaria-1200x800-1-500x500.jpg'
WHERE name = 'Omelette';

UPDATE kitcheners
SET name     = 'KEVIN',
    lastname = 'FRANCISCO'
WHERE name = 'John';



CREATE OR REPLACE FUNCTION best_waiters(since date, until date)
	RETURNS table (
		id          uuid,
		name        varchar,
		lastname    varchar,
		orders_made int
	)
AS
$$
BEGIN
	RETURN QUERY
		SELECT w.id ::uuid,
		       w.name ::varchar,
		       w.lastname ::varchar,
		       sum(1) ::int AS orders_taked
		FROM waiters w
			     INNER JOIN orders o ON w.id = o.take_by_id
		WHERE o.requested_on >= since
		  AND o.requested_on <= until
		GROUP BY w.id
		ORDER BY orders_taked DESC;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION best_waiters(since date, until date);

CREATE OR REPLACE FUNCTION best_kitcheners(since date, until date)
	RETURNS table (
		id          uuid,
		name        varchar,
		lastname    varchar,
		orders_made int
	)
AS
$$
BEGIN
	RETURN QUERY
		SELECT k.id::uuid,
		       k.name::varchar,
		       k.lastname::varchar,
		       sum(1)::int AS orders_made
		FROM kitcheners k
			     INNER JOIN order_details od ON k.id = od.made_by_id
		WHERE od.ready_on >= since
		  AND od.ready_on <= until
		GROUP BY k.id
		ORDER BY orders_made DESC;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION best_kitcheners(since date, until date);
SELECT * FROM best_waiters('2024-10-11', '2024-11-11');

CREATE OR REPLACE FUNCTION sales_report()
	RETURNS table (
		ingredient_id   uuid,
		ingredient_name varchar(255),
		quantity_used   numeric(10, 2),
		current_stock   numeric(10, 2),
		day_of_week     varchar(15),
		date            date,
		unit            varchar(15)
	)
AS
$$
BEGIN
	RETURN QUERY
		WITH recipe_table AS (SELECT i.id    AS id,
		                             i.name  AS name,
		                             i.unit  AS unit,
		                             i.stock AS stock,
		                             r.quantity
		                      FROM dishes d
			                           INNER JOIN recipes r ON d.id = r.dish_id
			                           INNER JOIN ingredients i ON r.ingredient_id = i.id)
		SELECT recipe_table.id::uuid                                AS ingredient_id,
		       recipe_table.name ::varchar(255)                     AS ingredient_name,
		       count(i.id) * recipe_table.quantity ::numeric(10, 2) AS quantity_used,
		       recipe_table.stock ::numeric(10, 2)                  AS current_stock,
		       to_char(od.ready_on, 'FMDay') ::varchar(15)          AS day_of_week,
		       od.ready_on ::date                                   AS date,
		       recipe_table.unit ::varchar(15)                      AS unit
		FROM order_details od
			     INNER JOIN recipes r ON od.dish_id = r.dish_id
			     INNER JOIN ingredients i ON r.ingredient_id = i.id
			     INNER JOIN recipe_table ON recipe_table.id = i.id
		GROUP BY recipe_table.id,
		         recipe_table.name,
		         recipe_table.unit,
		         recipe_table.quantity,
		         recipe_table.stock,
		         od.ready_on;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM sales_report();

SELECT * FROM order_details WHERE dish_id IS NULL;

CREATE OR REPLACE FUNCTION taken_tables()
	RETURNS table (
		table_number    int,
		current_process varchar,
		client_name     varchar,
		dish_id         uuid
	)
AS
$$
BEGIN
	RETURN QUERY
		SELECT o.table_number::int,
		       od.current_process::varchar,
		       o.client_name::varchar,
		       od.dish_id::uuid
		FROM orders o
			     INNER JOIN order_details od ON o.id = od.order_id
		WHERE od.current_process = 'WAITING_KITCHENER'
		   OR od.current_process = 'GETTING_READY'
		   OR od.current_process = 'READY_TO_DELIVER';
END;
$$
	LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION count_last_month_ingredients_used()
	RETURNS table (
		id              uuid,
		name            varchar,
		unit            varchar,
		stock           numeric(10, 2),
		last_month_used numeric(10, 2)
	)
AS
$$
BEGIN
	RETURN QUERY
		SELECT i.id ::uuid,
		       i.name ::varchar,
		       i.unit ::varchar,
		       i.stock ::numeric(10, 2),
		       sum(r.quantity) ::numeric(10, 2) AS last_month_used
		FROM order_details od
			     INNER JOIN recipes r ON od.dish_id = r.dish_id
			     INNER JOIN ingredients i ON r.ingredient_id = i.id
		WHERE od.ready_on >= date_trunc('month', current_date) - INTERVAL '1 month'
		  AND od.ready_on < date_trunc('month', current_date)
		GROUP BY i.id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION count_sales(request_date date)
	RETURNS int AS
$$
BEGIN
	RETURN (SELECT count(DISTINCT order_id) AS finalized_orders
	        FROM order_details
	        WHERE ready_on = request_date
		      AND current_process = 'FINISHED');
END;
$$ LANGUAGE plpgsql;

SELECT count(DISTINCT order_id) FROM order_details WHERE ready_on = '2024-11-11' AND current_process = 'FINISHED';

SELECT * FROM count_sales('2024-11-11');

DROP FUNCTION count_last_month_ingredients_used();

SELECT * FROM count_last_month_ingredients_used();

UPDATE ingredients SET stock = stock + 100 WHERE stock > -99;
