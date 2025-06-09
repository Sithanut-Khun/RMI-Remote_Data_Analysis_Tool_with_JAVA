Project Title: Remote Data Analysis Tool using Java RMI with a Swing-based GUI

📌 Objective:
To build a distributed Java application that enables remote data analysis using Java RMI (Remote Method Invocation). The project features both a client-side GUI (built with Swing) and a server-side processing module, simulating how data science workflows can be scaled and accessed remotely.

📦 Key Components:

- Java RMI for client-server communication
- Swing GUI for user-friendly input/output on the client side
- Server that receives input, performs simple data analysis, and returns results
- Data visualization using JFreeChart or another Java charting library

🏗 Project Structure:

rmi-data-analysis/
├── shared/     → RMI Interface (DataService.java)
├── server/     → RMI Server implementation and launcher
├── client/     → Client with both CLI and GUI options
└── utils/      → Tracking logging 

🔧 Functionalities:

- Accepts user input via GUI
- Sends input to server using RMI
- Server processes the data and returns results
- Client displays results in a GUI window

✅ Technologies Used:

- Java SE 8+
- Maven
- Java RMI
- Swing (for GUI)
- JFreeChart (for visual output)

📚 Learning Outcomes:

- Understanding of Java RMI architecture
- Practical experience with distributed systems
- GUI development with Swing
- Basics of client-server synchronization

🤝 Authors
👤 Khun Sithanut

🎓 Institute of Technology of Cambodia

📚 Department of Applied Mathematics and Statistics - Data Science Major
