CREATE OR REPLACE FUNCTION assign_order(order_id uuid, dish int, kitchener_id uuid)
    RETURNS boolean AS
$$
BEGIN
    IF NOT exists(SELECT 1 FROM kitcheners WHERE id = kitchener_id) THEN
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
    IF NOT exists(SELECT 1 FROM kitcheners WHERE id = kitchener_id) THEN
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

            IF NOT FOUND THEN
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