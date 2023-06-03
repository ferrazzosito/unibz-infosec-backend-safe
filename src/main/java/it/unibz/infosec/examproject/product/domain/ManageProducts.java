package it.unibz.infosec.examproject.product.domain;

import it.unibz.infosec.examproject.user.domain.ManageUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ManageProducts {

    private final ProductRepository productRepository;
    private SearchProducts searchProducts;
    private final ManageUsers manageUsers;

    @Autowired
    public ManageProducts(ProductRepository productRepository, ManageUsers manageUsers) {
        this.productRepository = productRepository;
        this.manageUsers = manageUsers;
    }

    private Product validateProduct (Long id) {
        final Optional<Product> maybeProduct = productRepository.findById(id);
        if (maybeProduct.isEmpty())
            throw new IllegalArgumentException("Product with id '" + id + "' does not exist yet!");
        return maybeProduct.get();
    }

    public Product createProduct(String name, int cost, Long vendorId) {
        return productRepository.save(new Product(name, cost, manageUsers.readUser(vendorId).getId()));
    }

    public Product readProduct(Long id) {
        return validateProduct(id);
    }

    public Product updateProduct(Long id, Long vendorId, String name, int cost) {
        final Product product = validateProduct(id);
        if (!vendorId.equals(product.getVendorId())) {
            throw new IllegalArgumentException("Only the owner of a product can update it");
        }
        product.setName(name);
        product.setCost(cost);
        return productRepository.save(product);
    }

    public Product deleteProduct(Long id) {
        final Product product = validateProduct(id);
        productRepository.delete(product);
        return product;
    }

    public List<Product> getByVendor(Long vendorId) {
        return productRepository.findByVendorId(vendorId);
    }
}
