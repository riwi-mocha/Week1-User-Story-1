import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MiniTiendaJOptionPane {

    private static final ArrayList<String> productNames = new ArrayList<>();
    private static double[] prices = new double[0];
    private static final HashMap<String, Integer> stock = new HashMap<>();
    private static double sessionTotal = 0.0;

    public static void main(String[] args) {
        while (true) {
            String option = JOptionPane.showInputDialog(null,
                    "MINI-TIENDA\n\n" +
                    "1) Agregar producto\n" +
                    "2) Listar inventario\n" +
                    "3) Comprar producto\n" +
                    "4) Mostrar estadisticas (mas barato y mas caro)\n" +
                    "5) Buscar producto por nombre\n" +
                    "6) Salir con ticket final\n\n" +
                    "Elige una opcion (1-6):",
                    "Menu", JOptionPane.QUESTION_MESSAGE);
            if (option == null) {
                showFinalTicketAndExit();
            }
            option = option != null ? option.trim() : "";
            switch (option) {
                case "1":
                    flowAddProduct();
                    break;
                case "2":
                    flowListInventory();
                    break;
                case "3":
                    flowBuyProduct();
                    break;
                case "4":
                    flowStats();
                    break;
                case "5":
                    flowSearch();
                    break;
                case "6":
                    showFinalTicketAndExit();
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Opcion invalida. Intenta de nuevo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static void flowAddProduct() {
        String name = promptNonEmptyString("Ingresa el nombre del producto:");
        if (name == null) return;
        if (indexOfNombre(name) != -1) {
            JOptionPane.showMessageDialog(null, "El producto ya existe. No se permiten duplicados.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Double price = promptPositiveDouble("Ingresa el precio del producto (numero positivo):");
        if (price == null) return;
        Integer qty = promptNonNegativeInt("Ingresa el stock inicial (entero >= 0):");
        if (qty == null) return;
        boolean ok = addProducto(name, price, qty);
        if (ok) {
            JOptionPane.showMessageDialog(null, "Producto agregado correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No se pudo agregar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void flowListInventory() {
        if (productNames.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay productos en el inventario.", "Inventario", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("INVENTARIO\n\n");
        for (int i = 0; i < productNames.size(); i++) {
            String n = productNames.get(i);
            double p = prices[i];
            int s = stock.getOrDefault(n, 0);
            sb.append((i + 1)).append(". ")
              .append(n).append(" | Precio: ").append(formatMoney(p))
              .append(" | Stock: ").append(s).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString(), "Inventario", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void flowBuyProduct() {
        if (productNames.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay productos disponibles para comprar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String name = promptNonEmptyString("Ingresa el nombre del producto a comprar:");
        if (name == null) return;
        int idx = indexOfNombre(name);
        if (idx == -1) {
            ArrayList<Integer> matches = findPartialMatches(name);
            if (matches.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No se encontro el producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            } else if (matches.size() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("Coincidencias encontradas:\n\n");
                for (int i : matches) {
                    sb.append("- ").append(productNames.get(i))
                      .append(" | Precio: ").append(formatMoney(prices[i]))
                      .append(" | Stock: ").append(stock.getOrDefault(productNames.get(i), 0))
                      .append("\n");
                }
                sb.append("\nEspecifica el nombre exacto.");
                JOptionPane.showMessageDialog(null, sb.toString(), "Buscar", JOptionPane.INFORMATION_MESSAGE);
                return;
            } else {
                idx = matches.get(0);
            }
        }
        String exactName = productNames.get(idx);
        int available = stock.getOrDefault(exactName, 0);
        if (available <= 0) {
            JOptionPane.showMessageDialog(null, "No hay stock disponible para este producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer qty = promptPositiveInt("Ingresa la cantidad a comprar (entero > 0). Disponible: " + available);
        if (qty == null) return;
        if (qty > available) {
            JOptionPane.showMessageDialog(null, "Stock insuficiente. Disponible: " + available, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double unit = prices[idx];
        double subtotal = unit * qty;
        int confirm = JOptionPane.showConfirmDialog(null,
                "Confirmar compra:\n\n" +
                "Producto: " + exactName + "\n" +
                "Cantidad: " + qty + "\n" +
                "Precio unitario: " + formatMoney(unit) + "\n" +
                "Subtotal: " + formatMoney(subtotal),
                "Confirmacion", JOptionPane.OK_CANCEL_OPTION);
        if (confirm == JOptionPane.OK_OPTION) {
            stock.put(exactName, available - qty);
            sessionTotal += subtotal;
            JOptionPane.showMessageDialog(null, "Compra realizada. Total acumulado: " + formatMoney(sessionTotal), "Exito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void flowStats() {
        if (productNames.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay productos para calcular estadisticas.", "Estadisticas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int minIdx = 0;
        int maxIdx = 0;
        for (int i = 1; i < productNames.size(); i++) {
            if (prices[i] < prices[minIdx]) minIdx = i;
            if (prices[i] > prices[maxIdx]) maxIdx = i;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ESTADISTICAS\n\n")
          .append("Mas barato: ").append(productNames.get(minIdx))
          .append(" | Precio: ").append(formatMoney(prices[minIdx]))
          .append(" | Stock: ").append(stock.getOrDefault(productNames.get(minIdx), 0)).append("\n\n")
          .append("Mas caro: ").append(productNames.get(maxIdx))
          .append(" | Precio: ").append(formatMoney(prices[maxIdx]))
          .append(" | Stock: ").append(stock.getOrDefault(productNames.get(maxIdx), 0));
        JOptionPane.showMessageDialog(null, sb.toString(), "Estadisticas", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void flowSearch() {
        if (productNames.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay productos para buscar.", "Buscar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String q = promptNonEmptyString("Ingresa texto para buscar por nombre (coincidencia parcial):");
        if (q == null) return;
        ArrayList<Integer> matches = findPartialMatches(q);
        if (matches.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Sin resultados.", "Buscar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Resultados de busqueda:\n\n");
        for (int i : matches) {
            String n = productNames.get(i);
            sb.append("- ").append(n)
              .append(" | Precio: ").append(formatMoney(prices[i]))
              .append(" | Stock: ").append(stock.getOrDefault(n, 0))
              .append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString(), "Buscar", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showFinalTicketAndExit() {
        JOptionPane.showMessageDialog(null,
                "TICKET FINAL\n\n" +
                "Total acumulado de compras: " + formatMoney(sessionTotal) + "\n\n" +
                "Gracias por usar la mini-tienda.",
                "Ticket", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private static boolean addProducto(String name, double price, int qty) {
        if (indexOfNombre(name) != -1) return false;
        expandPrecios(prices.length + 1);
        productNames.add(name);
        prices[productNames.size() - 1] = price;
        stock.put(name, qty);
        return true;
    }

    private static void expandPrecios(int newSize) {
        if (newSize <= prices.length) return;
        double[] newArr = new double[newSize];
        System.arraycopy(prices, 0, newArr, 0, prices.length);
        prices = newArr;
    }

    private static int indexOfNombre(String name) {
        for (int i = 0; i < productNames.size(); i++) {
            if (productNames.get(i).equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private static ArrayList<Integer> findPartialMatches(String text) {
        ArrayList<Integer> res = new ArrayList<>();
        String q = text.toLowerCase();
        for (int i = 0; i < productNames.size(); i++) {
            if (productNames.get(i).toLowerCase().contains(q)) res.add(i);
        }
        return res;
    }

    private static String promptNonEmptyString(String message) {
        while (true) {
            String in = JOptionPane.showInputDialog(null, message, "Entrada", JOptionPane.QUESTION_MESSAGE);
            if (in == null) return null;
            in = in.trim();
            if (!in.isEmpty()) return in;
            JOptionPane.showMessageDialog(null, "El valor no puede estar vacio.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Double promptPositiveDouble(String message) {
        while (true) {
            String in = JOptionPane.showInputDialog(null, message, "Entrada", JOptionPane.QUESTION_MESSAGE);
            if (in == null) return null;
            in = in.trim();
            if (in.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Ingresa un numero valido.", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            try {
                double v = Double.parseDouble(in);
                if (v > 0) return v;
                JOptionPane.showMessageDialog(null, "Debe ser un numero positivo.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Formato numerico invalido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static Integer promptNonNegativeInt(String message) {
        while (true) {
            String in = JOptionPane.showInputDialog(null, message, "Entrada", JOptionPane.QUESTION_MESSAGE);
            if (in == null) return null;
            in = in.trim();
            if (in.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Ingresa un entero valido.", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            try {
                int v = Integer.parseInt(in);
                if (v >= 0) return v;
                JOptionPane.showMessageDialog(null, "Debe ser un entero >= 0.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Formato numerico invalido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static Integer promptPositiveInt(String message) {
        while (true) {
            String in = JOptionPane.showInputDialog(null, message, "Entrada", JOptionPane.QUESTION_MESSAGE);
            if (in == null) return null;
            in = in.trim();
            if (in.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Ingresa un entero valido.", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            try {
                int v = Integer.parseInt(in);
                if (v > 0) return v;
                JOptionPane.showMessageDialog(null, "Debe ser un entero > 0.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Formato numerico invalido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String formatMoney(double v) {
        return String.format("$%,.2f", v).replace(",", "#").replace(".", ",").replace("#", ".");
    }
}
