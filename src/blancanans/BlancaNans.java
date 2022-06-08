package blancanans;

import static blancanans.BlancaNans.dormits;
import static blancanans.BlancaNans.perMenjar;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlancaNans {

    volatile static int dormits = 0;
    volatile static int perMenjar = 0;
    static final int NANS = 7;

    public static void main(String[] args) throws InterruptedException {
        MonitorBN monitor = new MonitorBN();
        Thread[] threads = new Thread[NANS + 1];
        for (int i = 0; i < NANS; i++) {
            threads[i] = new Thread(new Nan(i,monitor));
            threads[i].start();
        }
        threads[NANS] = new Thread(new BlancaNeus(monitor));
        threads[NANS].start();
        for (int i = 0; i < NANS+1; i++) {
            threads[i].join();
        }
        System.out.println("Acaba la simulació");
    }
}

class MonitorBN {

    Lock lock = new ReentrantLock();
    Condition menjar = lock.newCondition();
    Condition cadira = lock.newCondition();
    int numCadires = 4;

    void donarMenjar() {
        lock.lock();
        try {
            perMenjar--;
            menjar.signal();
        } finally {
            lock.unlock();
        }
    }
    
    void demanarMenjar() {
        lock.lock();
        try {
            perMenjar++;
            menjar.await();
        } catch (InterruptedException ex) {
        } finally {
            lock.unlock();
        }
    }

    void obtenirCadira() {
        lock.lock();
        try {
            while (numCadires == 0) {
                cadira.await();
            }
            numCadires--;
        } catch (InterruptedException ex) {
        } finally {
            lock.unlock();
        }
    }

    void alliberarCadira() {
        lock.lock();
        try {
            numCadires++;
            cadira.signal();
        } finally {
            lock.unlock();
        }
    }
    
    synchronized void dormir(){
        dormits++;
    }

}

class Nan implements Runnable {
    int id;
    MonitorBN monitor;

    public Nan(int id, MonitorBN monitor) {
        this.id = id;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        for (int i = 0; i < 2; i++) {
            monitor.obtenirCadira();
            System.out.println("La Nan "+id+" té cadira i ha demanat menjar");
            monitor.demanarMenjar();
            menjar(id);
            monitor.alliberarCadira();
            System.out.println("La Nan "+id+" ha alliberat un cadira");
        }
        monitor.dormir();
    }

    void menjar(int id) {
        System.out.println("La Nan "+id+" està menjant");
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
    }

}

class BlancaNeus implements Runnable {
    MonitorBN monitor;

    public BlancaNeus(MonitorBN monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        while (dormits < 7) {
            if (perMenjar == 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
            } else {
                cuinar();
                monitor.donarMenjar();
                System.out.println("Na Blancaneus ha servit menjar");
            }
        }
    }
    
    void cuinar() {
        System.out.println("Na Blancaneus està cuinant");
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
    }
}
