package kg.kanztovary.kanztovarybackend.domain.enums;

/**
 * Варианты сортировки каталога товаров.
 * Передаётся как query-параметр: ?sortBy=PRICE_ASC
 */
public enum ProductSortBy {
    PRICE_ASC,
    PRICE_DESC,
    NAME_ASC,
    NAME_DESC,
    NEWEST,
    OLDEST
}