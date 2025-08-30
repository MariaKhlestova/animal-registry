package animalregistry;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Animal {
    protected int id;
    protected String name;
    protected LocalDate birthDate;
    protected List<String> commands;
    
    public Animal(int id, String name, LocalDate birthDate) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.commands = new ArrayList<>();
    }
    
    public Animal(int id, String name, LocalDate birthDate, String commandsStr) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.commands = new ArrayList<>();
        if (commandsStr != null && !commandsStr.trim().isEmpty()) {
            for (String cmd : commandsStr.split(",")) {
                this.commands.add(cmd.trim());
            }
        }
    }
    
    public void addCommand(String command) {
        commands.add(command);
    }
    
    public List<String> getCommands() {
        return new ArrayList<>(commands);
    }
    
    public String getCommandsAsString() {
        return String.join(", ", commands);
    }
    
    public int getAge() {
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    public String getName() { return name; }
    public LocalDate getBirthDate() { return birthDate; }
    public int getId() { return id; }
}

class DomesticAnimal extends Animal {
    public DomesticAnimal(int id, String name, LocalDate birthDate, String commands) {
        super(id, name, birthDate, commands);
    }
}

class Dog extends DomesticAnimal {
    public Dog(int id, String name, LocalDate birthDate, String commands) {
        super(id, name, birthDate, commands);
    }
}

class Cat extends DomesticAnimal {
    public Cat(int id, String name, LocalDate birthDate, String commands) {
        super(id, name, birthDate, commands);
    }
}

class Hamster extends DomesticAnimal {
    public Hamster(int id, String name, LocalDate birthDate, String commands) {
        super(id, name, birthDate, commands);
    }
}

class PackAnimal extends Animal {
    public PackAnimal(int id, String name, LocalDate birthDate, String commands) {
        super(id, name, birthDate, commands);
    }
}

class Horse extends PackAnimal {
    public Horse(int id, String name, LocalDate birthDate, String commands) {
        super(id, name, birthDate, commands);
    }
}

class Camel extends PackAnimal {
    public Camel(int id, String name, LocalDate birthDate, String commands) {
        super(id, name, birthDate, commands);
    }
}

class Donkey extends PackAnimal {
    public Donkey(int id, String name, LocalDate birthDate, String commands) {
        super(id, name, birthDate, commands);
    }
}

class Counter implements AutoCloseable {
    private int count = 0;
    private boolean closed = false;
    private boolean resourceUsed = false;
    
    public void add() {
        if (closed) {
            throw new IllegalStateException("Счетчик закрыт");
        }
        count++;
        resourceUsed = true; 
    }
    
    public int getCount() {
        return count;
    }
    
    @Override
    public void close() {
        closed = true;
        if (!resourceUsed) {
            throw new IllegalStateException("Счетчик не был использован в try-with-resources");
        }
    }
}

class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/Друзья_человека";
    private static final String USER = "root";
    private static final String PASSWORD = "123"; 
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Подключение к базе данных успешно!");
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
            System.out.println("Убедитесь, что:");
            System.out.println("1. MySQL запущен: sudo service mysql start");
            System.out.println("2. База 'Друзья_человека' существует");
            System.out.println("3. Пароль правильный в DatabaseConnection классе");
        }
    }
}

public class AnimalRegistry {
    private List<Animal> animals = new ArrayList<>();
    private int nextId = 1;
    private Scanner scanner = new Scanner(System.in);
    
    public void run() {
        initializeDatabase();
        loadAnimalsFromDatabase();
        
        boolean running = true;
        
        while (running) {
            System.out.println("\n=== Реестр животных ===");
            System.out.println("1. Завести новое животное");
            System.out.println("2. Показать список команд животного");
            System.out.println("3. Обучить животное новой команде");
            System.out.println("4. Показать всех животных");
            System.out.println("5. Обновить данные из базы");
            System.out.println("6. Выход");
            System.out.print("Выберите опцию: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
            } catch (Exception e) {
                choice = 0;
            }
            scanner.nextLine(); 
            
            switch (choice) {
                case 1:
                    addNewAnimal();
                    break;
                case 2:
                    showAnimalCommands();
                    break;
                case 3:
                    teachNewCommand();
                    break;
                case 4:
                    showAllAnimals();
                    break;
                case 5:
                    loadAnimalsFromDatabase();
                    break;
                case 6:
                    running = false;
                    System.out.println("Выход из программы...");
                    break;
                default:
                    System.out.println("Неверный выбор");
            }
        }
        
        scanner.close();
    }
    
    private void initializeDatabase() {
        System.out.println("Инициализация подключения к базе данных...");
        DatabaseConnection.testConnection();
    }
    
    private void loadAnimalsFromDatabase() {
        animals.clear();
        String sql = "SELECT a.*, va.type as animal_type, 'Вьючное' as category " +
                    "FROM Животные a " +
                    "JOIN Вьючные_животные va ON a.id = va.animal_id " +
                    "UNION ALL " +
                    "SELECT a.*, da.type as animal_type, 'Домашнее' as category " +
                    "FROM Животные a " +
                    "JOIN Домашние_животные da ON a.id = da.animal_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                LocalDate birthDate = rs.getDate("birth_date").toLocalDate();
                String commands = rs.getString("commands");
                String type = rs.getString("animal_type");
                String category = rs.getString("category");
                
                Animal animal = createAnimal(id, name, birthDate, commands, type, category);
                if (animal != null) {
                    animals.add(animal);
                    if (id >= nextId) nextId = id + 1;
                }
            }
            
            System.out.println("Загружено " + animals.size() + " животных из базы данных");
            
        } catch (SQLException e) {
            System.out.println("Ошибка загрузки из базы: " + e.getMessage());
        }
    }
    
    private Animal createAnimal(int id, String name, LocalDate birthDate, String commands, String type, String category) {
        switch (type) {
            case "Собака": return new Dog(id, name, birthDate, commands);
            case "Кошка": return new Cat(id, name, birthDate, commands);
            case "Хомяк": return new Hamster(id, name, birthDate, commands);
            case "Лошадь": return new Horse(id, name, birthDate, commands);
            case "Верблюд": return new Camel(id, name, birthDate, commands);
            case "Осел": return new Donkey(id, name, birthDate, commands);
            default: 
                System.out.println("Неизвестный тип животного: " + type);
                return null;
        }
    }
    
    private void addNewAnimal() {
        try (Counter counter = new Counter()) {
            System.out.println("\n--- Добавление нового животного ---");
            
            System.out.print("Введите имя: ");
            String name = scanner.nextLine();
            if (name.trim().isEmpty()) {
                System.out.println("Имя не может быть пустым!");
                return;
            }
            
            System.out.print("Введите дату рождения (гггг-мм-дд): ");
            LocalDate birthDate;
            try {
                birthDate = LocalDate.parse(scanner.nextLine());
            } catch (Exception e) {
                System.out.println("Неверный формат даты! Используйте гггг-мм-дд");
                return;
            }
            
            System.out.println("Выберите тип животного:");
            System.out.println("1. Собака (Домашнее)");
            System.out.println("2. Кошка (Домашнее)");
            System.out.println("3. Хомяк (Домашнее)");
            System.out.println("4. Лошадь (Вьючное)");
            System.out.println("5. Верблюд (Вьючное)");
            System.out.println("6. Осел (Вьючное)");
            System.out.print("Выберите: ");
            
            int typeChoice;
            try {
                typeChoice = scanner.nextInt();
            } catch (Exception e) {
                typeChoice = 0;
            }
            scanner.nextLine();
            
            if (typeChoice < 1 || typeChoice > 6) {
                System.out.println("Неверный выбор типа животного");
                return;
            }
            
            System.out.print("Введите базовые команды (через запятую): ");
            String basicCommands = scanner.nextLine();
            
            String type = "";
            String category = "";
            
            switch (typeChoice) {
                case 1: type = "Собака"; category = "Домашнее"; break;
                case 2: type = "Кошка"; category = "Домашнее"; break;
                case 3: type = "Хомяк"; category = "Домашнее"; break;
                case 4: type = "Лошадь"; category = "Вьючное"; break;
                case 5: type = "Верблюд"; category = "Вьючное"; break;
                case 6: type = "Осел"; category = "Вьючное"; break;
            }
            
            int animalId = saveAnimalToDatabase(name, birthDate, basicCommands, type, category);
            
            if (animalId > 0) {
                Animal animal = createAnimal(animalId, name, birthDate, basicCommands, type, category);
                if (animal != null) {
                    animals.add(animal);
                    nextId = animalId + 1;
                    counter.add();
                    System.out.println("Животное успешно добавлено! ID: " + animalId);
                }
            } else {
                System.out.println("Ошибка при добавлении животного в базу");
            }
            
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int saveAnimalToDatabase(String name, LocalDate birthDate, String commands, String type, String category) {
        String sqlAnimal = "INSERT INTO Животные (name, birth_date, commands) VALUES (?, ?, ?)";
        int animalId = -1;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlAnimal, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, name);
            pstmt.setDate(2, Date.valueOf(birthDate));
            pstmt.setString(3, commands);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        animalId = rs.getInt(1);
                        saveAnimalType(animalId, type, category);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Ошибка сохранения в базу: " + e.getMessage());
        }
        
        return animalId;
    }
    
    private void saveAnimalType(int animalId, String type, String category) {
        String tableName = category.equals("Домашнее") ? "Домашние_животные" : "Вьючные_животные";
        String sql = "INSERT INTO " + tableName + " (animal_id, type) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, animalId);
            pstmt.setString(2, type);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Ошибка сохранения типа животного: " + e.getMessage());
        }
    }
    
    private void showAnimalCommands() {
        if (animals.isEmpty()) {
            System.out.println("Нет животных в реестре");
            return;
        }
        
        System.out.println("\nСписок животных:");
        for (int i = 0; i < animals.size(); i++) {
            Animal animal = animals.get(i);
            System.out.println((i + 1) + ". " + animal.getName() + 
                             " (ID: " + animal.getId() + 
                             ", Возраст: " + animal.getAge() + " лет)");
        }
        
        System.out.print("Выберите животное: ");
        int choice;
        try {
            choice = scanner.nextInt();
        } catch (Exception e) {
            choice = 0;
        }
        scanner.nextLine();
        
        if (choice > 0 && choice <= animals.size()) {
            Animal animal = animals.get(choice - 1);
            System.out.println("Команды " + animal.getName() + ": " + animal.getCommands());
        } else {
            System.out.println("Неверный выбор");
        }
    }
    
    private void teachNewCommand() {
        if (animals.isEmpty()) {
            System.out.println("Нет животных в реестре");
            return;
        }
        
        System.out.println("\nСписок животных:");
        for (int i = 0; i < animals.size(); i++) {
            Animal animal = animals.get(i);
            System.out.println((i + 1) + ". " + animal.getName() + 
                             " (ID: " + animal.getId() + 
                             ", Команд: " + animal.getCommands().size() + ")");
        }
        
        System.out.print("Выберите животное: ");
        int choice;
        try {
            choice = scanner.nextInt();
        } catch (Exception e) {
            choice = 0;
        }
        scanner.nextLine();
        
        if (choice > 0 && choice <= animals.size()) {
            Animal animal = animals.get(choice - 1);
            System.out.print("Введите новую команду: ");
            String newCommand = scanner.nextLine().trim();
            
            if (!newCommand.isEmpty()) {
                animal.addCommand(newCommand);
                updateAnimalCommandsInDatabase(animal);
                System.out.println("Команда '" + newCommand + "' добавлена для " + animal.getName() + "!");
            } else {
                System.out.println("Команда не может быть пустой");
            }
        } else {
            System.out.println("Неверный выбор");
        }
    }
    
    private void updateAnimalCommandsInDatabase(Animal animal) {
        String sql = "UPDATE Животные SET commands = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, animal.getCommandsAsString());
            pstmt.setInt(2, animal.getId());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Ошибка обновления команд в базе: " + e.getMessage());
        }
    }
    
    private void showAllAnimals() {
        if (animals.isEmpty()) {
            System.out.println("Нет животных в реестре");
            return;
        }
        
        System.out.println("\nВсе животные");
        System.out.println("Всего животных: " + animals.size());
        System.out.println("=" .repeat(60));
        
        for (Animal animal : animals) {
            String type = animal.getClass().getSimpleName();
            System.out.println(type + ": " + animal.getName() + 
                             "\n   Возраст: " + animal.getAge() + " лет" +
                             "\n   Команды: " + animal.getCommands() +
                             "\n   ID: " + animal.getId());
            System.out.println("-" .repeat(40));
        }
    }
    
    public static void main(String[] args) {
        AnimalRegistry registry = new AnimalRegistry();
        registry.run();
    }
}