package com.luguosong.ssiach11ex6.services;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class NameService {

//    @RolesAllowed("ROLE_ADMIN")
    @Secured("ROLE_ADMIN")
    public String getName() {
        return "Fantastico";
    }
}
