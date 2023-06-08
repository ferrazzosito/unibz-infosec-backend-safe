package it.unibz.infosec.examproject.order.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibz.infosec.examproject.product.domain.ManageProducts;
import it.unibz.infosec.examproject.product.domain.Product;
import it.unibz.infosec.examproject.user.domain.ManageUsers;
import it.unibz.infosec.examproject.user.domain.SafeUserEntity;
import it.unibz.infosec.examproject.user.domain.UserEntity;
import it.unibz.infosec.examproject.util.crypto.hashing.Hashing;
import it.unibz.infosec.examproject.util.crypto.rsa.RSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ManageOrders {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    private final OrderRepository orderRepository;
    private final ManageUsers manageUsers;
    private final ManageProducts manageProducts;
    private SearchOrders searchOrders;

    @Autowired
    public ManageOrders(OrderRepository orderRepository, ManageUsers manageUsers, ManageProducts manageProducts) {
        this.orderRepository = orderRepository;
        this.manageUsers = manageUsers;
        this.manageProducts = manageProducts;
    }

    private Order validateOrder(Long id) {
        final Optional<Order> maybeOrder = orderRepository.findById(id);
        if (maybeOrder.isEmpty()) {
            throw new IllegalArgumentException("Order with id '" + id + "' does not exist yet!");
        }
        return maybeOrder.get();
    }

    public Order createOrder(Long productId, Long clientId) {
        UserEntity client = manageUsers.readUser(clientId);
        Product product = manageProducts.readProduct(productId);

        if (client.getBalance() < product.getCost()) {
            throw new IllegalArgumentException("Insufficient balance to place this order!");
        }

        String orderDocument =
                "ID: " + product.getId() + "\n" +
                "NAME: " + product.getName() + "\n" +
                "COST: " + product.getCost() + "\n" +
                "VENDOR: " + product.getVendorId() + "\n" +
                "BUYER: " + client.getId();

        String digest = Hashing.getDigest(orderDocument);
        byte[] DSA = RSA.encrypt(digest, client.getPrivateKey(), client.getNKey());

        return orderRepository.save(new Order(productId, product.getVendorId(), clientId, orderDocument, DSA));
    }

    public Order approveOrder(Long idOrder) throws Exception {
        final Order order = readOrder(idOrder);
        final Product product = manageProducts.readProduct(order.getProductId());
        final UserEntity customer = manageUsers.readUser(order.getClientId());
        final boolean orderValidity = isSignatureOrderValid(order.getId());

        if (!orderValidity) {
            throw new Exception("Order digital signature is not valid!");
        }
        if (customer.getBalance() < product.getCost()) {
            throw new IllegalArgumentException("Insufficient balance to place this order!");
        }

        manageUsers.updateUser(order.getClientId(), Math.negateExact(product.getCost()));
        order.setApproved(true);
        return orderRepository.save(order);
    }

    public boolean isSignatureOrderValid(Long idOrder) {
        final Order order = readOrder(idOrder);
        final UserEntity client = manageUsers.readUser(order.getClientId());
        final byte[] DSA = order.getDSA();
        final String retrievedDigest = RSA.decryptToString(DSA, client.getPublicKey(), client.getNKey());
        final String computedDigest = Hashing.getDigest(order.getOrderDocument());

        Logger.getLogger("ManageOrders").log(Level.INFO, "computedDigest = " + computedDigest + " retrievedDigest = " + retrievedDigest);
        return retrievedDigest.equals(computedDigest);
    }

    public Order readOrder(Long id) {
        return validateOrder(id);
    }

    public Order deleteOrder(Long id, Long customer) {
        final Order order = validateOrder(id);
        if (!order.getClientId().equals(customer)) {
            throw new IllegalArgumentException("Only the holder of an order can delete it");
        }
        orderRepository.delete(order);
        return order;
    }

    public List<Order> getByCustomer(Long customerId) {
        return orderRepository.findByClientId(customerId).stream().map(o ->
                new OrderWithProductInfo(o,
                        manageProducts.readProduct(o.getProductId())
                )).collect(Collectors.toList());
    }

    public List<Order> getByVendor(Long vendorId) {
        return orderRepository.findByVendorId(vendorId).stream().map(o -> {
            final UserEntity customer = manageUsers.readUser(o.getClientId());
            final Product product = manageProducts.readProduct(o.getProductId());
            return new OrderWithCustomerAndProductInfo(o,
                    new SafeUserEntity(
                        customer.getEmail(),
                        customer.getRole().getName(),
                        customer.getBalance()),
                    product);
        }).collect(Collectors.toList());
    }

    public List<Order> getByProduct(Long productId) {
        return orderRepository.findByProductId(productId);
    }

    public List<Order> getApprovedByVendor(Long vendorId) {
        return orderRepository.findByVendorIdAndIsApprovedTrue(vendorId);
    }

    public List<Order> getToBeApprovedByVendor(Long vendorId) {
        return orderRepository.findByVendorIdAndIsApprovedFalse(vendorId);
    }
}
