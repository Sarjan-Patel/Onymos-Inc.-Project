# Lock-Free Stock Trading Matching Engine
This project implements a real-time stock trading engine that matches Buy and Sell orders based on defined criteria. It is designed to handle 1,024 stock tickers, process orders efficiently in O(n) time, and ensure thread safety using lock-free data structures (AtomicReference) instead of traditional locking mechanisms.
