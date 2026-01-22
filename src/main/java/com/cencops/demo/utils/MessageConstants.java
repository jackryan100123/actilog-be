package com.cencops.demo.utils;

public final class MessageConstants {

    private MessageConstants() {
    }

    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    public static final String ROLE_UPDATED = "User role updated successfully";

    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong";

    public static final String ADMIN_MODIFICATION_RESTRICTED = "Admins are not authorized to modify Super Admin accounts";
    public static final String ACTION_NOT_ALLOWED = "Action not allowed on own account";

    public static final String UNAUTHORIZED ="Unauthorized";
    public static final String ACCESS_DENIED = "Access Denied";
    public static final String JWT_INVALID_OR_EXPIRED = "Invalid or expired JWT token";

    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String BAD_REQUEST = "Bad request";

    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logged out successfully";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";

    public static final String USER_CREATED = "User created successfully";
    public static final String USER_UPDATED = "User updated successfully";
    public static final String USER_DELETED = "User deleted successfully";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_NOT_FOUND_ID = "User not found with id: ";
    public static final String USER_NOT_FOUND_USERNAME = "User not found with username: ";
    public static final String USERNAME_EXISTS = "Username already exists";

    public static final String ACCOUNT_INACTIVE = "Your account is inactive. Please contact admin";

    public static final String PASSWORD_UPDATED = "Password updated successfully";
    public static final String PASSWORD_SAME_AS_OLD = "New password cannot be same as old password";
    public static final String PASSWORD_INCORRECT = "Old password is incorrect";

    public static final String USER_STATUS_UPDATED = "User status updated to ";
    public static final String INVALID_STATUS = "Invalid status provided";

    public static final String ACTIVITY_NOT_FOUND = "Activity not found";
    public static final String ACTIVITY_ALREADY_PRESENT = "Daily activity already created for today";
    public static final String ACTIVITY_CREATED = "Daily activity created successfully";
    public static final String ACTIVITY_UPDATED = "Daily activity updated successfully";
    public static final String ACTIVITY_DELETED = "Daily activity deleted successfully";
    public static final String EDIT_AFTER_DAY_END = "Editing is not allowed after the day this activity was posted.";
    public static final String DELETE_AFTER_DAY_END = "Deleting is allowed only until the end of the day the activity was created.";

    public static final String ATTACHMENT_EMPTY = "Attachment file cannot be empty";

    public static final String NOTICE_CREATED ="Notice updated for category: ";
    public static final String MANUAL_ADDED ="Manual uploaded";
    public static final String MANUAL_REMOVED ="Manual removed";
}
