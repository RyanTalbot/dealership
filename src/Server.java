import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/*
The server starts and creates an opening on port 8080
and waits for a client to connect. Upon connection,
confirmation is shown then we execute the thread pool.

Clients are handled by a separate class which uses the
runnable interface. This class receives the input from
a client then calls the corresponding command.

A car and car inventory class is also housed in this
file.

The server must be started before a client(s) can connect.
 */

public class Server {
    /*
    Port needed to connect, this can be changed if in use
    on the users machine, but it must be changed on the client
    side also.

    We also create an array list of clients which manages the
    threads, we also create a thread pool of size 50.
     */
    private static final int PORT = 8080;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(50);

    public static void main(String[] args) throws IOException {
        /*
        We first create a server socket and pass the port number,
        we then accept the connection and show confirmation on
        the server side.

        We then create a client handler object and handle the
        client and their requests to the server.

        Clients are added to the clients array list then
        we execute the thread pool.
         */
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("[SERVER] Waiting for connection...");

        while (true) {
            Socket client = listener.accept();
            System.out.println("[SERVER] Connected to client");

            ClientHandler clientThread = new ClientHandler(client);

            clients.add(clientThread);
            pool.execute(clientThread);
        }
    }
}

class ClientHandler implements Runnable {
    /*
    This class handles' client requests to the
    server and manages the thread access via
    semaphores.
     */
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private static Inventory inventory = new Inventory();
    private static Semaphore semaphore = new Semaphore(1);

    public ClientHandler(Socket clientSocket) throws IOException {
        this.client = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
    }

    @Override
    public void run() {
        /*
        We take the client command and then call
        the corresponding method.
         */

        try {
            while (true) {
                String request = in.readLine();

                if (request.contains("add")) {
                    add();
                } else if (request.contains("sell")) {
                    sell();
                } else if (request.contains("info")) {
                    carInfo();
                } else if (request.contains("commands")) {
                    showCommands();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            out.close();
            try {
                in.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void add() throws IOException {
        /*
        This method takes input from the user
        who wants to add a car to the inventory.
        Data is entered separated by commas, then
        split into an array, then finally passed
        into the car objects constructor. Upon
        creating, the new car is then added
        to the inventory.
         */
        String[] details;

        while (true) {
            out.println("Enter Car Details");
            String givenDetails = in.readLine();
            // Split by comma and store it in the array
            details = givenDetails.split(",");
            break;
        }

        // Extracting info
        String reg = details[0];
        String make = details[1];
        int price = Integer.parseInt(details[2]);
        int mileage = Integer.parseInt(details[3]);
        boolean forSale = Boolean.parseBoolean(details[4]);

        Car car = new Car(reg, make, price, mileage, forSale);
        inventory.getInventory().put(car.getRegistration(), car);
    }

    public void sell() throws IOException, InterruptedException {
        /*
        To sell a car it first must exist. We check this by
        taking input from the user who will pass the registration.
        If the car exists then it is sold and removed from the
        inventory.

        Only 1 client can sell the same car at a time so here
        we use a semaphore to manage access.

        Confirmation of a successful or unsuccessful sale
        is then shown to the user.
         */
        while (true) {
            out.println("Enter Registration");
            out.print("> ");
            String givenReg = in.readLine();

            if (inventory.getInventory().containsKey(givenReg) && inventory.getInventory().get(givenReg).isForSale()) {
                semaphore.acquire();
                inventory.getInventory().remove(givenReg);
                semaphore.release();
                out.println(givenReg + " SOLD!");
            } else {
                out.println(givenReg + " Isn't For Sale...");
            }
            break;
        }
    }

    public void carInfo() throws IOException {
        /*
        Similarly to the sell method, we take input
        from the user which will be the registration
        as this is the key in the hash map.

        We then search the hash map and return the
        car.
         */
        while (true) {
            out.println("Enter Registration");
            String givenReg = in.readLine();

            if (inventory.getInventory().containsKey(givenReg)) {
                out.println(List.of(
                        inventory.getInventory().get(givenReg).getRegistration(),
                        inventory.getInventory().get(givenReg).getMake(),
                        Integer.toString(inventory.getInventory().get(givenReg).getPrice()),
                        Integer.toString(inventory.getInventory().get(givenReg).getMileage()),
                        Boolean.toString(inventory.getInventory().get(givenReg).isForSale())));
                break;
            }
            break;
        }
    }

    public void showCommands() {
        out.println("Commands-[add, sell, info, commands, stop]");
    }
}

class Car {
    /*
    Standard car object with car details as well
    as their getter and setter methods.
     */
    private String registration, make;
    private int price, mileage;
    private boolean forSale;

    public Car(String registration, String make, int price, int mileage, boolean forSale) {
        this.registration = registration;
        this.make = make;
        this.price = price;
        this.mileage = mileage;
        this.forSale = forSale;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }
}

class Inventory {
    /*
    An inventory of cars stored in a hash map.
    The key is the registration and the value is
    the car object itself.
     */
    HashMap<String, Car> inventory;

    public Inventory() {
        this.inventory = new HashMap<>();

        Car nissan = new Car("12D-32454", "Nissan", 15000, 500, true);
        Car honda = new Car("06D-12547", "Honda", 2500, 100000, true);
        Car toyota = new Car("17D-65468", "Toyota", 12500, 6500, true);
        Car mitsubishi = new Car("95G-16432", "Mitsubishi", 5000, 65000, true);
        Car audi = new Car("13D-87943", "Audi", 27750, 11250, false);
        Car bmw = new Car("16D-16582", "BMW", 38750, 63250, true);
        Car volkswagen = new Car("17G-15687", "Volkswagen", 30000, 25000, false);
        Car mercedes = new Car("19L-16886", "Mercedes", 45000, 45000, false);
        Car seat = new Car("05L-99856", "Seat", 6000, 36750, true);
        Car ferrari = new Car("94K-21368", "Ferrari", 125000, 0, true);
        Car bentley = new Car("20C-56166", "Bentley", 75000, 500, true);
        Car vauxhall = new Car("15G-63355", "Vauxhall", 7500, 18500, false);
        Car lotus = new Car("11D-78965", "Lotus", 22500, 11000, true);
        Car ford = new Car("09C-65423", "Ford", 20000, 56188, true);
        Car chevrolet = new Car("11K-98897", "Chevrolet", 12000, 500, true);

        inventory.put(nissan.getRegistration(), nissan);
        inventory.put(honda.getRegistration(), honda);
        inventory.put(toyota.getRegistration(), toyota);
        inventory.put(mitsubishi.getRegistration(), mitsubishi);
        inventory.put(audi.getRegistration(), audi);
        inventory.put(bmw.getRegistration(), bmw);
        inventory.put(volkswagen.getRegistration(), volkswagen);
        inventory.put(mercedes.getRegistration(), mercedes);
        inventory.put(seat.getRegistration(), seat);
        inventory.put(ferrari.getRegistration(), ferrari);
        inventory.put(bentley.getRegistration(), bentley);
        inventory.put(vauxhall.getRegistration(), vauxhall);
        inventory.put(lotus.getRegistration(), lotus);
        inventory.put(ford.getRegistration(), ford);
        inventory.put(chevrolet.getRegistration(), chevrolet);
    }

    public HashMap<String, Car> getInventory() {
        return inventory;
    }
}