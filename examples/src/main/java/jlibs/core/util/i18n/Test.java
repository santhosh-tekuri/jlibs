package jlibs.core.util.i18n;

/**
 * @author Santhosh Kumar T
 */
@Bundle({
    @Entry("this is comment"),
    @Entry(lhs="keyName", rhs="rhsValue")
})
public class Test{
    @Bundle({
        @Entry("this is comment"),
        @Entry(hint=Hint.DISPLAY_NAME, rhs="this is field1")
    })
    private String field1;

    @Bundle({
        @Entry("this is comment"),
        @Entry(hintName="customHint", rhs="this is field2")
    })
    private String field2;

    @Bundle({
        @Entry("{0} - id specified"),
        @Entry(lhs="invalidID", rhs="you have specified invalid id {0}")
    })
    public void test(){

    }
}
