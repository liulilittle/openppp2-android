package supersocksr.ppp.android.openppp2;

import java.util.concurrent.CountDownLatch;

public class Awaitable {
    private final CountDownLatch lk_ = new CountDownLatch(1);
    private Object tag_ = null;

    public void tag(Object v) {
        this.tag_ = v;
    }

    public Object tag() {
        return this.tag_;
    }

    public boolean processed() {
        try {
            lk_.countDown();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean await() {
        try {
            lk_.await();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}