Lock-Free Stock Trading Matching Engine
Overview
This project implements a real-time stock trading engine that matches Buy and Sell orders based on defined criteria. It is designed to handle 1,024 stock tickers, process orders efficiently in O(n) time, and ensure thread safety using lock-free data structures (AtomicReference) instead of traditional locking mechanisms.

Features & Functionalities
Adding Orders (addOrder)
Supports adding Buy and Sell orders for up to 1,024 tickers.
Uses a lock-free linked list approach for storing orders without using dictionaries/maps.
Implements an atomic, thread-safe insertion method (insertOrder).
Matching Orders (matchOrder)
Matches Buy orders with the lowest-priced available Sell orders.
Ensures that Buy price is greater than or equal to the lowest Sell price for a successful trade.
Uses an O(n) time complexity approach (linear search through Sell orders).
Trades are printed in the format:
bash
Copy
Edit
[TRADE] Ticker=358 Price=148 Qty=740
Lock-Free & Thread Safety
The system avoids mutex locks by using AtomicReference for safe multi-threaded access.
Order lists for Buy and Sell are singly linked lists, manipulated using lock-free CAS (Compare-and-Swap) operations.
Ensures no race conditions while multiple threads modify the order book.
No Use of Maps or Dictionaries
All tickers are stored in a fixed-size array (orderBook[1024]), preventing the use of maps/dictionaries.
Buy and Sell orders are stored as linked lists, preventing the need for key-value storage structures.
Order Execution Simulation (simulateOrders)
Generates random orders dynamically.
Randomly selects ticker, price, quantity, and order type (BUY or SELL).
Automatically calls matchOrder() after every BUY order to simulate real-time trading.
Runs 500 transactions by default in the main function.
