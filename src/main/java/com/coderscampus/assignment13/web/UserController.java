package com.coderscampus.assignment13.web;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import com.coderscampus.assignment13.domain.Account;
import com.coderscampus.assignment13.domain.Address;
import com.coderscampus.assignment13.service.AccountService;
import com.coderscampus.assignment13.service.AddressService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.coderscampus.assignment13.domain.User;
import com.coderscampus.assignment13.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UserController {
	

	private final UserService userService;
	private final AddressService addressService;
	private final AccountService accountService;

	public UserController(UserService userService, AddressService addressService, AccountService accountService) {
		this.userService = userService;
		this.addressService = addressService;
		this.accountService = accountService;
	}

	@GetMapping("/register")
	public String getCreateUser (ModelMap model) {
		
		model.put("user", new User());
		
		return "register";
	}
	
	@PostMapping("/register")
	public String postCreateUser (User user) {
		System.out.println(user);
		userService.saveUser(user);
		return "redirect:/register";
	}
	
	@GetMapping("/users")
	public String getAllUsers (ModelMap model) {
		Set<User> users = userService.findAll();
		
		model.put("users", users);
		if (users.size() == 1) {
			model.put("user", users.iterator().next());
		}
		
		return "users";
	}
	
	@GetMapping("/users/{userId}")
	public String getOneUser (ModelMap model, @PathVariable Long userId) {
		User user = userService.findById(userId);
		model.put("users", Arrays.asList(user));
		model.put("user", user);
		return "users";
	}
	
	@PostMapping("/users/{userId}")
	public String postOneUser (@PathVariable Long userId, User targetUser) {
		User sourceUser = userService.findById(userId);
		targetUser.setAccounts(sourceUser.getAccounts());
		Address persistedAddress = addressService.saveAddress(targetUser.getAddress());
		targetUser.setAddress(persistedAddress);
		userService.saveUser(targetUser);
		return "redirect:/users/"+targetUser.getUserId();
	}
	
	@PostMapping("/users/{userId}/delete")
	public String deleteOneUser (@PathVariable Long userId) {
		userService.delete(userId);
		return "redirect:/users";
	}

	@GetMapping("/users/{userId}/account/{accountId}")
	public String showAccount(@PathVariable Long accountId, ModelMap model) {
		Optional<Account> accountOpt = this.accountService.getAccountById(accountId);
		model.put("account",accountOpt.orElse(null));
		return "account";
	}

	@PostMapping("/users/{userId}/account/{accountId}")
	public String updateUserAccount(@PathVariable Long accountId,@PathVariable Long userId, Account targetAccount){
		this.accountService.saveAccount(targetAccount);
		return "redirect:/users/"+userId+"/account/" + accountId;
	}

	@PostMapping("/users/{userId}/accounts")
	public String createAccount(@PathVariable Long userId){
		User persistedUser = this.userService.findById(userId);
		Account account = new Account();
		account.setAccountName("Account #" + (persistedUser.getAccounts().size()+1));
		persistedUser.getAccounts().add(account);
		account.getUsers().add(persistedUser);
		Account persistedAccount = this.accountService.saveAccount(account);
		return "redirect:/users/"+userId+"/account/"+persistedAccount.getAccountId();
	}
}
