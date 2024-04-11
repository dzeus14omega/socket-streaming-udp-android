package com.lilincpp.github.libezftp.user;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.usermanager.impl.WritePermission;

public enum EZFtpUserPermission {

    WRITE(new WritePermission());

    private Authority authority;

    public Authority getAuthority() {
        return authority;
    }

    EZFtpUserPermission(Authority authority) {
        this.authority = authority;
    }
}
