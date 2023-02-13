import java.io.*;
import java.util.*;
import java.util.concurrent.*;
class Thread_Level_2 extends Thread{
    String order;
    int id;
    Semaphore sem;
    Scanner myReader;
    public Thread_Level_2(String order, int id, Semaphore sem, Scanner myReader){
        this.order = order;
        this.id = id;
        this.sem = sem;
        this.myReader = myReader;
    }

    public synchronized String read_in_parallel(){
        if(myReader.hasNext())
            return myReader.nextLine();
        else
            return null;
    }

    public void run(){
        try {
            Tema2.sem1.acquire();
            while (true) {
                synchronized (Thread_Level_2.class) {
                    String data = read_in_parallel();
                    if (data != null) {
                        String[] product = (data.split(","));
                        if(product[0].equals(order)){
                            String result =  data +  ",shipped\n";
                            synchronized (Tema2.myWriter2) {
                                try {
                                    Tema2.myWriter2.write(result);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    else break;
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Tema2.sem1.release();
    }
}

public class Tema2 extends Thread{
    public int id;
    static Scanner myReader;
    public String products_file;

    public Tema2(int id, Scanner myReader, String products_file) {
        this.id = id;
        this.myReader = myReader;
        this.products_file = products_file;
    }

    static FileWriter myWriter1;
    static FileWriter myWriter2;
    static int nr_threads;
    // semaforul folosit la nivelul 2 de paralelizare
    static Semaphore sem1;


    public synchronized String read_in_parallel(){
        if(myReader.hasNext())
            return myReader.nextLine();
        else
            return null;
    }

    public void run(){
        while (true) {
            synchronized (Tema2.class) {
                String data = read_in_parallel();
                if (data != null) {
                    String[] order = (data.split(","));
                    String ID = order[0];
                    int nr = Integer.parseInt(order[1]);
                    String nr2 = order[1];
                    Thread_Level_2[] threads = new Thread_Level_2[nr];
                    File file1 = new File(products_file);
                    Scanner reader = null;
                    try {
                        reader = new Scanner(file1);
                        for(int j = 0; j < nr; j++) {
                            threads[j] = new Thread_Level_2(ID, j, sem1, reader);
                            threads[j].start();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                    // inchidere thread-uri nivel 2
                    for (int k = 0; k < nr; k++) {
                        try {
                            threads[k].join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (nr2.charAt(0) != '0') {
                        String result = data + ",shipped\n";
                        try {
                            myWriter1.write(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                else break;
            }
        }

    }
    public static void main(String[] args) throws IOException, InterruptedException {
        myWriter1 = new FileWriter("orders_out.txt");
        myWriter2 = new FileWriter("order_products_out.txt");

        String name = args[0];
        nr_threads = Integer.parseInt(args[1]);
        sem1 = new Semaphore(nr_threads);
        File file1 = new File(name + "/orders.txt");
        myReader = new Scanner(file1);
        Tema2[] threads = new Tema2[nr_threads];
        for(int i = 0; i < nr_threads; i++){
            threads[i] = new Tema2(i, myReader, name + "/order_products.txt");
            threads[i].start();
        }
        // inchidere thread-uri nivel 1
        for(int i = 0; i < nr_threads; i++){
            threads[i].join();
        }
        myWriter1.close();
        myWriter2.close();

    }
}
