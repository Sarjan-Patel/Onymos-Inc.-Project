import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeMatchingEngine {

    // Constants for the stock trading system
    private static final int TOTAL_TICKERS = 1024;  // Number of supported stock tickers
    private static final int MAX_ORDER_QTY = 1000;  // Maximum allowed quantity per order
    private static final int MAX_ORDER_PRICE = 500; // Maximum allowed price per order

    // Enum to define order type: Buy or Sell
    enum OrderType {
        BUY,
        SELL
    }

    // Represents an order in the trading system (linked list node)
    static class Order {
        final OrderType orderType; // Buy or Sell
        final int tickerId;        // Stock ticker identifier (0-1023)
        int quantity;              // Order quantity
        final int price;           // Order price

        // Pointer to the next order in the list
        AtomicReference<Order> nextOrder;

        public Order(OrderType orderType, int tickerId, int quantity, int price) {
            this.orderType = orderType;
            this.tickerId = tickerId;
            this.quantity = quantity;
            this.price = price;
            this.nextOrder = new AtomicReference<>(null);
        }
    }

    // Structure to manage buy and sell order lists for each ticker
    static class TickerOrderBook {
        AtomicReference<Order> buyOrders;  // Head of the Buy order linked list
        AtomicReference<Order> sellOrders; // Head of the Sell order linked list

        public TickerOrderBook() {
            buyOrders = new AtomicReference<>(null);
            sellOrders = new AtomicReference<>(null);
        }
    }

    // The global order book (array of ticker order books)
    private static final TickerOrderBook[] orderBook = new TickerOrderBook[TOTAL_TICKERS];

    // Initialize the order book for all tickers
    static {
        for (int i = 0; i < TOTAL_TICKERS; i++) {
            orderBook[i] = new TickerOrderBook();
        }
    }

    // Insert an order at the head of the linked list (lock-free)
    private static void insertOrder(AtomicReference<Order> head, Order newOrder) {
        while (true) {
            Order currentHead = head.get();
            newOrder.nextOrder.set(currentHead);

            // Attempt to insert the order at the head of the list
            if (head.compareAndSet(currentHead, newOrder)) {
                return;
            }
            // Retry if insertion fails due to concurrent updates
        }
    }

    // Remove a specific order from the linked list (lock-free)
    private static boolean removeOrder(AtomicReference<Order> head, Order orderToRemove) {
        while (true) {
            Order previousOrder = null;
            Order currentOrder = head.get();

            // Traverse the linked list to find the order
            while (currentOrder != null && currentOrder != orderToRemove) {
                previousOrder = currentOrder;
                currentOrder = currentOrder.nextOrder.get();
            }

            if (currentOrder == null) {
                // Order already removed or not found
                return false;
            }

            Order nextOrder = currentOrder.nextOrder.get();

            if (previousOrder == null) {
                // Removing the first order in the list
                if (head.compareAndSet(currentOrder, nextOrder)) {
                    return true;
                }
            } else {
                // Removing a non-head order
                if (previousOrder.nextOrder.compareAndSet(currentOrder, nextOrder)) {
                    return true;
                }
            }
        }
    }

    // Add a new order to the appropriate list (Buy or Sell)
    public static void addOrder(OrderType orderType, int tickerId, int quantity, int price) {
        Order newOrder = new Order(orderType, tickerId, quantity, price);

        // Insert into the appropriate linked list (Buy or Sell)
        if (orderType == OrderType.BUY) {
            insertOrder(orderBook[tickerId].buyOrders, newOrder);
        } else {
            insertOrder(orderBook[tickerId].sellOrders, newOrder);
        }
    }

    // Match a new buy order with existing sell orders
    public static void matchOrder(int tickerId, int buyQuantity, int buyPrice) {
        AtomicReference<Order> sellListHead = orderBook[tickerId].sellOrders;
        Order currentOrder = sellListHead.get();

        while (currentOrder != null && buyQuantity > 0) {
            int sellPrice = currentOrder.price;
            int sellQuantity = currentOrder.quantity;
            Order nextOrder = currentOrder.nextOrder.get();

            // Check if the sell price is within the acceptable range for the buy order
            if (sellPrice <= buyPrice) {
                boolean isRemoved = removeOrder(sellListHead, currentOrder);
                if (isRemoved) {
                    // Determine the quantity to be traded
                    int tradedQuantity = Math.min(buyQuantity, sellQuantity);
                    buyQuantity -= tradedQuantity;

                    // Log the trade execution
                    System.out.println("[TRADE] Ticker=" + tickerId
                            + " Price=" + sellPrice
                            + " Qty=" + tradedQuantity);
                }
                // Move to the next order
                currentOrder = nextOrder;
            } else {
                // No match found, continue searching
                currentOrder = nextOrder;
            }
        }
    }

    // Simulates order creation and matching with random parameters
    public static void simulateOrders(int orderCount) {
        Random random = new Random();

        for (int i = 0; i < orderCount; i++) {
            int tickerId = random.nextInt(TOTAL_TICKERS);
            int quantity = 1 + random.nextInt(MAX_ORDER_QTY);
            int price = 1 + random.nextInt(MAX_ORDER_PRICE);
            OrderType orderType = (random.nextBoolean()) ? OrderType.BUY : OrderType.SELL;

            // Add new order
            addOrder(orderType, tickerId, quantity, price);

            // If the order is a buy, try to match it immediately
            if (orderType == OrderType.BUY) {
                matchOrder(tickerId, quantity, price);
            }
        }
    }

    // Main function to execute the simulation
    public static void main(String[] args) {
        simulateOrders(500);
    }
}
