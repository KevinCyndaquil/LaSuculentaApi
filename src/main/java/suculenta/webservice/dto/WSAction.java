package suculenta.webservice.dto;

public enum WSAction {
    /**
     * Ocurre cuando un mesero completa una order con exito, entonces se le notifica al chef para que comience a
     * realizar el pedido. El chef debe actualizar la tabla.
     */
    NEW_ORDER,
    /**
     * El chef ha terminado de realizar el pedido, entonces se le notifica al mesero para que la entregue,
     * le devuelve también el orden.
     */
    FINISH_ORDER,
    /**
     * Este servidor realiza una petición a la IA de maguchi, entonces redirecciono la respuesta a los
     * clientes.
     */
    NEW_PREDICTION,
}
