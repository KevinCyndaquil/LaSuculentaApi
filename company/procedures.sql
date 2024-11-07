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
		FOR UPDATE;

	UPDATE order_details od
	SET made_by_id      = kitchener_id,
	    current_process = 'GETTING_READY'
	WHERE od.order_id = assign_order.order_id
	  AND cns = dish
	  AND current_process = 'WAITING_KITCHENER';

	RETURN FOUND;
END;
$$
	LANGUAGE plpgsql;


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

--SELECT * FROM sell_dishes_table();

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
		GROUP BY o.id;
END;
$$ LANGUAGE plpgsql;

--SELECT * FROM sold_orders('2024-01-01', '2024-12-31');

CREATE OR REPLACE FUNCTION best_waiters(since date, until date)
	RETURNS setof waiters AS
$$
BEGIN
	RETURN QUERY
		SELECT w.*, sum(1) AS orders_taked
		FROM waiters w
			     INNER JOIN orders o ON w.id = o.take_by_id
		WHERE o.requested_on >= since
		  AND o.requested_on <= until
		GROUP BY w.id
		ORDER BY orders_taked DESC;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION best_kitcheners(since date, until date)
	RETURNS setof kitcheners AS
$$
BEGIN
	RETURN QUERY
		SELECT k.*, sum(1) AS orders_made
		FROM kitcheners k
			     INNER JOIN order_details od ON k.id = od.made_by_id
		WHERE od.ready_on >= since
		  AND od.ready_on <= until
		GROUP BY k.id
		ORDER BY orders_made DESC;
END;
$$ LANGUAGE plpgsql;
