package org.rug.web;

import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.function.BiFunction;

@RestController
public class ShutdownController implements ApplicationContextAware {

    private ApplicationContext context;

    @RequestMapping(value = "/die", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String shutdownContext(@RequestParam(value = "password", defaultValue = "") String password) {
        if (password.equals("doit")){
            java.lang.System.out.println("Yes, master!");
            ((ConfigurableApplicationContext) context).close();
            return "Yes, master!";
        }else {
            return "Ah ah ah, you didn't say the magic word!";
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;
    }

}