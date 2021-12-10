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

public class Server {
    private static final int PORT = 8080;

    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws IOException {
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
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private static Inventory inventory = new Inventory();

    public ClientHandler(Socket clientSocket) throws IOException {
        this.client = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String request = in.readLine();

                if (request.contains("add")) {
                    out.println("ADD");
                    add();
                } else if (request.contains("sell")) {
                    out.println("SELL");
                    sell();
                } else if (request.contains("info")) {
                    out.println("INFO");
                    carInfo();
                }
            }
        } catch (IOException e) {
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
        String[] details;

        while (true) {
            out.println("Enter Car Details");
            String givenDetails = in.readLine();

            details = givenDetails.split(",");
            break;
        }

        String reg = details[0];
        String make = details[1];
        int price = Integer.parseInt(details[2]);
        int mileage = Integer.parseInt(details[3]);
        boolean forSale = Boolean.parseBoolean(details[4]);

        Car car = new Car(reg, make, price, mileage, forSale);

        inventory.getInventory().put(car.getRegistration(), car);
    }

    public void sell() throws IOException {
        while (true) {
            out.println("Enter Registration");
            out.print("> ");
            String givenReg = in.readLine();

            if (inventory.getInventory().containsKey(givenReg) && inventory.getInventory().get(givenReg).isForSale()) {
                out.println(givenReg + " SOLD!");
            } else {
                out.println(givenReg + " Isn't For Sale...");
            }
            break;
        }
    }

    public void carInfo() throws IOException {
           while (true) {
               out.println("Enter Registration");
               String givenReg = in.readLine();

               if (inventory.getInventory().containsKey(givenReg)) {
                   out.println(List.of(inventory.getInventory().get(givenReg).getMake(),
                           inventory.getInventory().get(givenReg).getRegistration(),
                           Integer.toString(inventory.getInventory().get(givenReg).getPrice()),
                           Integer.toString(inventory.getInventory().get(givenReg).getMileage()),
                           Boolean.toString(inventory.getInventory().get(givenReg).isForSale())));

                   break;
               }
               break;
           }
    }
}

class Car {
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
    HashMap<String, Car> inventory;

    public Inventory() {
        this.inventory = new HashMap<>();

        Car nissan = new Car("12D-32454", "Nissan", 15000, 500, true);
        Car honda = new Car("06D-12547", "Honda", 2500, 100000, true);
        Car toyota = new Car("17D-65468", "Toyota", 12500, 6500, true);

        inventory.put(nissan.getRegistration(), nissan);
        inventory.put(honda.getRegistration(), honda);
        inventory.put(toyota.getRegistration(), toyota);
    }

    public HashMap<String, Car> getInventory() {
        return inventory;
    }
}