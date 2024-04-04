package com.viettel.generaltestandroid.ftpserver;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.util.Collections;

public class UserManager implements org.apache.ftpserver.ftplet.UserManager {
    private final String homeDirectory;

    public UserManager(File homeDirectory) {
        this.homeDirectory = homeDirectory.getAbsolutePath();
    }

    @Override
    public User getUserByName(String username) throws FtpException {
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setHomeDirectory(homeDirectory);
        user.setEnabled(true);
        user.setAuthorities(Collections.singletonList(new WritePermission()));
        return user;
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        return new String[0];
    }

    @Override
    public void delete(String s) throws FtpException {

    }

    @Override
    public void save(User user) throws FtpException {

    }

    @Override
    public boolean doesExist(String s) throws FtpException {
        return false;
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
        return null;
    }

    @Override
    public String getAdminName() throws FtpException {
        return null;
    }

    @Override
    public boolean isAdmin(String s) throws FtpException {
        return false;
    }

}
