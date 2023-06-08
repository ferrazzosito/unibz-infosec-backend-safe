package it.unibz.infosec.examproject.user.application;

import it.unibz.infosec.examproject.user.domain.*;
import it.unibz.infosec.examproject.util.RESTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/users")
public class UserController {

    private final UserRepository userRepository;
    private final ManageUsers manageUsers;
    private final SearchUsers searchUsers;

    @Autowired
    public UserController(UserRepository userRepository, ManageUsers manageUsers, SearchUsers searchUsers) {
        this.userRepository = userRepository;
        this.manageUsers = manageUsers;
        this.searchUsers = searchUsers;
    }

    @GetMapping("/{id}")
    public UserEntity findById(@PathVariable("id") Long id) {
        final UserEntity user = manageUsers.readUser(id);
        // return new SafeUserEntity(user.getEmail(), user.getRole().getName());
        return user;
    }

    @GetMapping("/me")
    public UserEntity getSelf() {
        return manageUsers.readUser(RESTUtils.getLoggedUser(userRepository).getId());
    }

    @PostMapping("/update")
    public UserEntity updateUser(@RequestBody UpdateUserDTO dto) {
        return manageUsers.updateUser(
                RESTUtils.getLoggedUser(userRepository).getId(), dto.getBalance());
    }

    @PostMapping("/topup")
    public SafeUserEntity updateUser(@RequestBody TopUpDTO topUpDTO) {
        final UserEntity updated = manageUsers.updateUser(
                RESTUtils.getLoggedUser(userRepository).getId(),
                    topUpDTO.getBalanceIncrease());
        return new SafeUserEntity(updated.getEmail(),
                updated.getRole().getName(), updated.getBalance());
    }

    @DeleteMapping("/delete/{id}")
    public UserEntity deleteUser(@PathVariable("id") Long id)  {
        return manageUsers.deleteUser(id);
    }

    @GetMapping("/getAll")
    public List<UserEntity> findAll(){
        return searchUsers.findAll();
    }

    @PostMapping("/transfer/{id}/{email}/{amount}")
    public UserEntity transferMoney(@PathVariable("id") Long id, @PathVariable("email") String email, @PathVariable("amount") int amount) {
        return manageUsers.sendAmount(id, email, amount);
    }
}
