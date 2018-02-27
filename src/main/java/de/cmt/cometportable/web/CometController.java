package de.cmt.cometportable.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CometController {

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResponseEntity<?> testController() {
        return new ResponseEntity<>("COMET", HttpStatus.OK);
    }
}
