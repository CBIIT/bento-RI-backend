package gov.nih.nci.bento.utility;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StrUtilTest {

    @Test
    public void getBoolText_Test() {
        assertThat(StrUtil.getBoolText(null), is(""));
        assertThat(StrUtil.getBoolText("TESTTESTTEST true TESTTESTTEST"), is("true"));
        assertThat(StrUtil.getBoolText("TEST_FALSE_TEST"), is(""));
        assertThat(StrUtil.getBoolText("TRUEFALSETESTTEST"), is(""));
        assertThat(StrUtil.getBoolText("TESTTESTTESTTESTTESTTEST true"), is("true"));
        assertThat(StrUtil.getBoolText("true"), is("true"));
        assertThat(StrUtil.getBoolText("false"), is("false"));
        assertThat(StrUtil.getBoolText(" false "), is("false"));
        assertThat(StrUtil.getBoolText(" FALse "), is("false"));
        assertThat(StrUtil.getBoolText(" tRue "), is("true"));
    }

    @Test
    public void getIntText_Test() {
        assertThat(StrUtil.getIntText(null), is(""));
        assertThat(StrUtil.getIntText("TESTTESTTEST 000 TESTTESTTEST"), is("000"));
        assertThat(StrUtil.getIntText("55TEST_FALSE_TEST"), is(""));
        assertThat(StrUtil.getIntText("TRUEFALSETESTTEST"), is(""));
        assertThat(StrUtil.getIntText("TESTTESTTESTTESTTESTTEST 1"), is("1"));
        assertThat(StrUtil.getIntText("1234"), is("1234"));
        assertThat(StrUtil.getIntText("98 "), is("98"));
        assertThat(StrUtil.getIntText(" 5 "), is("5"));
    }

/*    @Test
    public void getToken_Test() {
        assertThat(StrUtil.getToken(null), is(""));
        assertThat(StrUtil.getToken("Bearer "), is(""));
        assertThat(StrUtil.getToken("Bearer    ssss   "), is(""));
        assertThat(StrUtil.getToken("Bearer"), is(""));
        assertThat(StrUtil.getToken("Bearer xxxx"), is("xxxx"));
        assertThat(StrUtil.getToken("XXBearer xxxx"), is(""));
        assertThat(StrUtil.getToken("XXX xxxx"), is(""));
        assertThat(StrUtil.getToken("Bearerxxxx"), is(""));
        assertThat(StrUtil.getToken("Bearer 1234"), is("1234"));
        assertThat(StrUtil.getToken("Bearer 1*23/4"), is("1*23/4"));
        assertThat(StrUtil.getToken("bearer 1*23/4"), is(""));
    }*/

}