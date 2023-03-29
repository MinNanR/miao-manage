package site.minnan.miao.infrastructure.exception;

/**
 * 非法操作异常
 *
 * @author Minnan on 2023/03/29
 */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(){
        super();
    }

    public InvalidOperationException(String message){
        super(message);
    }
}
