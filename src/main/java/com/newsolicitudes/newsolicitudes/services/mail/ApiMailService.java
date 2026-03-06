package com.newsolicitudes.newsolicitudes.services.mail;

import java.util.Map;

public interface APiMailService {

    void enviarMail(String to, String subject,String templateName, Map<String,Object> body) ;

}
