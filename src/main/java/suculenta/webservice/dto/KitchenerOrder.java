package suculenta.webservice.dto;

import suculenta.webservice.model.Kitchener;
import suculenta.webservice.model.Order;

public record KitchenerOrder(Order.Process process, Kitchener kitchener) {
}
