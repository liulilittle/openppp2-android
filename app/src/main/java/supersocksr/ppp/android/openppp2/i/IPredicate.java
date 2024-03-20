package supersocksr.ppp.android.openppp2.i;

public interface IPredicate<T> {
    boolean handle(T item);
}