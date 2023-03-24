/*
package gov.nih.nci.bento.services;

import gov.nih.nci.bento.service.TokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith( SpringRunner.class )
@SpringBootTest
public class TokenTest {

    @Autowired
    TokenService tokenService;

    @Test
    public void getBoolText_Test() {
        // Expired Token
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImloZWF2ZW4wMTI5QGdtYWlsLmNvbSIsIklEUCI6Ikdvb2dsZSIsImZpcnN0TmFtZSI6Imtub2Nra25vY2siLCJpYXQiOjE2Nzg3MjEzOTMsImV4cCI6MTY3ODcyMzE5M30.274utW7rXDsym5XaWB6NBlWBHsyO4oXdFvAk8oXma80";
        assertThat(tokenService.verifyToken(token), is(false));
        // Null Token
        assertThat(tokenService.verifyToken(null), is(false));
        // Random Token
        assertThat(tokenService.verifyToken("token"), is(false));
        // empty
        assertThat(tokenService.verifyToken(""), is(false));
    }
}
*/
