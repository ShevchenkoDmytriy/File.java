import java.io.*;
import java.util.*;

class Banknote {
    private int denomination;
    private int quantity;

    public Banknote(int denomination, int quantity) {
        this.denomination = denomination;
        this.quantity = quantity;
    }

    public int getDenomination() {
        return denomination;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

class Card {
    private String number;
    private String pin;
    private int balance;

    public Card(String number, String pin, int balance) {
        this.number = number;
        this.pin = pin;
        this.balance = balance;
    }

    public boolean checkPin(String pin) {
        return this.pin.equals(pin);
    }

    public int getBalance() {
        return balance;
    }

    public void withdraw(int amount) {
        this.balance -= amount;
    }

    public String getNumber() {
        return number;
    }
}

class ATM {
    private List<Banknote> cash;
    private List<Card> cards;

    public ATM() {
        this.cash = new ArrayList<>();
        this.cards = new ArrayList<>();
        loadCash();
        loadCards();
    }

    private void loadCash() {
        cash.add(new Banknote(100, 10));
        cash.add(new Banknote(200, 5));

    }

    private void loadCards() {
        cards.add(new Card("1234567890123456", "1234", 1000));
    }

    public Card authorize(String number, String pin) {
        for (Card card : cards) {
            if (card.getNumber().equals(number) && card.checkPin(pin)) {
                return card;
            }
        }
        return null;
    }

    public void displayBalance(Card card) {
        System.out.println("Ваш баланс: " + card.getBalance());
    }

    public void withdraw(Card card, int amount) {
        if (amount <= 0 || amount > card.getBalance()) {
            System.out.println("Недопустимая сумма для снятия.");
            return;
        }
        if (canDispense(amount)) {
            updateCash(amount);
            card.withdraw(amount);
            System.out.println("Вы сняли " + amount + ". Ваш новый баланс: " + card.getBalance());
            logOperation("Снятие наличных: " + amount);
        } else {
            System.out.println("Недостаточно средств в банкомате для выдачи запрошенной суммы.");
        }
    }

    private boolean canDispense(int amount) {
        int totalAmount = 0;
        for (Banknote banknote : cash) {
            totalAmount += banknote.getDenomination() * banknote.getQuantity();
        }

        if (totalAmount < amount) {
            return false;
        }
        Map<Integer, Integer> toDispense = new HashMap<>();
        int remainingAmount = amount;
        for (Banknote banknote : cash) {
            int denomination = banknote.getDenomination();
            int availableNotes = banknote.getQuantity();
            if (availableNotes > 0 && denomination <= remainingAmount) {
                int notesNeeded = Math.min(remainingAmount / denomination, availableNotes);
                remainingAmount -= notesNeeded * denomination;
                toDispense.put(denomination, notesNeeded);
            }
        }

        return remainingAmount == 0;
    }

    private void updateCash(int amount) {
        int remainingAmount = amount;
        for (Banknote banknote : cash) {
            int denomination = banknote.getDenomination();
            int availableNotes = banknote.getQuantity();
            if (availableNotes > 0 && denomination <= remainingAmount) {
                int notesNeeded = Math.min(remainingAmount / denomination, availableNotes);
                remainingAmount -= notesNeeded * denomination;
                banknote.setQuantity(availableNotes - notesNeeded);
            }
        }
    }


    private void logOperation(String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("atm_log.txt", true))) {
            bw.write(message + "\n");
        } catch (IOException e) {
            System.out.println("Ошибка записи в файл лога: " + e.getMessage());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        ATM atm = new ATM();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Введите номер карты:");
            String cardNumber = br.readLine();
            System.out.println("Введите пин-код:");
            String pin = br.readLine();

            Card card = atm.authorize(cardNumber, pin);
            if (card != null) {
                atm.displayBalance(card);
                System.out.println("Введите сумму для снятия:");
                int amount = Integer.parseInt(br.readLine());
                atm.withdraw(card, amount);
            } else {
                System.out.println("Неверный пин-код или номер карты");
            }
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
