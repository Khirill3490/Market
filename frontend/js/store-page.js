const STORE_PAGE_SIZE = 21;

document.addEventListener("DOMContentLoaded", function () {
    console.log("store-page.js loaded");

    const productsGrid = document.getElementById("store-products-grid");
    const productsCount = document.getElementById("store-products-count");
    const pagination = document.getElementById("store-pagination");

    if (!productsGrid) {
        console.error("Products grid not found");
        return;
    }

    console.log("Products grid found:", productsGrid);

    loadProducts({
        productsGrid: productsGrid,
        productsCount: productsCount,
        pagination: pagination,
        page: 0
    });
});

async function loadProducts(view) {
    console.log("Start loading products... Page:", view.page);

    showLoading(view);

    try {
        const data = await apiGet(`/api/v1/products/?page=${view.page}&size=${STORE_PAGE_SIZE}`);

        console.log("Products response:", data);
        console.log("Products list:", data.content);

        renderProducts(view.productsGrid, data.content);
        renderProductsCount(view.productsCount, data);
        renderPagination(view, data);
    } catch (error) {
        console.error("Failed to load products:", error);

        renderError(view.productsGrid);
        renderProductsCount(view.productsCount, null);
        clearPagination(view.pagination);
    }
}

function showLoading(view) {
    view.productsGrid.innerHTML = `
        <div class="col-12">
            <p>Загрузка товаров...</p>
        </div>
    `;

    if (view.productsCount) {
        view.productsCount.textContent = "Загрузка товаров...";
    }

    clearPagination(view.pagination);
}

function renderProducts(productsGrid, products) {
    if (!products || products.length === 0) {
        productsGrid.innerHTML = `
            <div class="col-12">
                <div class="alert alert-warning">
                    Товары не найдены.
                </div>
            </div>
        `;
        return;
    }

    productsGrid.innerHTML = products
        .map(function (product) {
            return createProductCard(product);
        })
        .join("");
}

function createProductCard(product) {
    const publicId = product.publicId || "";
    const name = product.name || product.art || "Товар";
    const brand = product.brand || "Бренд не указан";
    const imageUrl = product.img || "images/items/1.jpg";
    const price = formatPrice(product.price);
    const stockQuantity = product.stockQuantity ?? 0;

    const detailUrl = publicId
        ? `./product-detail.html?publicId=${encodeURIComponent(publicId)}`
        : "#";

    return `
        <div class="col-md-4">
            <figure class="card card-product-grid">
                <div class="img-wrap">
                    <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(name)}">
                </div>

                <figcaption class="info-wrap">
                    <div class="fix-height">
                        <a href="${detailUrl}" class="title">
                            ${escapeHtml(name)}
                        </a>

                        <div class="text-muted small mt-1">
                            ${escapeHtml(brand)}
                        </div>

                        <div class="price-wrap mt-2">
                            <span class="price">${price}</span>
                        </div>

                        <div class="text-muted small">
                            В наличии: ${stockQuantity}
                        </div>
                    </div>

                    <a href="${detailUrl}" class="btn btn-block btn-primary">
                        Подробнее
                    </a>
                </figcaption>
            </figure>
        </div>
    `;
}

function renderProductsCount(productsCount, data) {
    if (!productsCount) {
        return;
    }

    if (!data) {
        productsCount.textContent = "Не удалось загрузить товары";
        return;
    }

    const totalElements = data.totalElements ?? 0;
    const currentPage = data.number ?? 0;
    const pageSize = data.size ?? STORE_PAGE_SIZE;
    const numberOfElements = data.numberOfElements ?? 0;

    if (totalElements === 0 || numberOfElements === 0) {
        productsCount.textContent = "Товары не найдены";
        return;
    }

    const from = currentPage * pageSize + 1;
    const to = from + numberOfElements - 1;

    productsCount.textContent = `Показано ${from}–${to} из ${totalElements} товаров`;
}

function renderPagination(view, data) {
    const pagination = view.pagination;

    if (!pagination) {
        return;
    }

    const totalPages = data.totalPages ?? 0;
    const currentPage = data.number ?? 0;

    if (totalPages <= 1) {
        clearPagination(pagination);
        return;
    }

    const pageItems = [];

    pageItems.push(createPageItem("Назад", currentPage - 1, false, currentPage === 0));

    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);

    for (let page = startPage; page <= endPage; page++) {
        pageItems.push(createPageItem(String(page + 1), page, page === currentPage, false));
    }

    pageItems.push(createPageItem("Вперёд", currentPage + 1, false, currentPage >= totalPages - 1));

    pagination.innerHTML = pageItems.join("");

    pagination.querySelectorAll("[data-page]").forEach(function (link) {
        link.addEventListener("click", function (event) {
            event.preventDefault();

            const targetPage = Number(link.dataset.page);

            loadProducts({
                productsGrid: view.productsGrid,
                productsCount: view.productsCount,
                pagination: view.pagination,
                page: targetPage
            });
        });
    });
}

function createPageItem(label, page, active, disabled) {
    const activeClass = active ? "active" : "";
    const disabledClass = disabled ? "disabled" : "";
    const dataPage = disabled ? "" : `data-page="${page}"`;

    return `
        <li class="page-item ${activeClass} ${disabledClass}">
            <a class="page-link" href="#" ${dataPage}>
                ${label}
            </a>
        </li>
    `;
}

function clearPagination(pagination) {
    if (pagination) {
        pagination.innerHTML = "";
    }
}

function renderError(productsGrid) {
    productsGrid.innerHTML = `
        <div class="col-12">
            <div class="alert alert-danger">
                Не удалось загрузить товары. Попробуйте позже.
            </div>
        </div>
    `;
}

function formatPrice(price) {
    if (price === null || price === undefined) {
        return "Цена не указана";
    }

    return `${Number(price).toLocaleString("ru-RU")} ₽`;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}