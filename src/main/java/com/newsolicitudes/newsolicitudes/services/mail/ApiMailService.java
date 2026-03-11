package com.newsolicitudes.newsolicitudes.services.mail;

import java.util.Map;

public interface ApiMailService {

    void enviarMail(String to, String subject,String templateName, Map<String,Object> body) ;

}
