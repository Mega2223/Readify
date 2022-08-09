import net.mega2223.readify.windows.TimeSpanSelector;

public class TimeSpanSelectorTest {
    public static void main(String[] args) {
        TimeSpanSelector selector = new TimeSpanSelector();
        selector.addConclusionTask(() -> System.out.println(selector.getTimeInSeconds()));
    }
}
