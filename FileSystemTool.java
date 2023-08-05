import java.util.*;

class FileSystemItem {
    String name;
    long mtime; // Modification time
    long size;

    public FileSystemItem(String name, long mtime, long size) {
        this.name = name;
        this.mtime = mtime;
        this.size = size;
    }
}

class FileSystemNode {
    Map<String, FileSystemNode> children;
    FileSystemItem item;

    public FileSystemNode() {
        children = new HashMap<>();
    }
}

class FileSystemTrie {
    FileSystemNode root;

    public FileSystemTrie() {
        root = new FileSystemNode();
    }

    public void add(String path, FileSystemItem item) {
        String[] pathParts = path.split("/");
        FileSystemNode curr = root;
        for (String part : pathParts) {
            if (!curr.children.containsKey(part)) {
                curr.children.put(part, new FileSystemNode());
            }
            curr = curr.children.get(part);
        }
        curr.item = item;
    }
public long getSize(String path) {
        FileSystemNode node = search(path);
        return (node != null) ? getSizeUnderNode(node) : 0;
    }

    private long getSizeUnderNode(FileSystemNode node) {
        if (node == null) {
            return 0;
        }

        long size = (node.item != null) ? node.item.size : 0;
        for (FileSystemNode child : node.children.values()) {
            size += getSizeUnderNode(child);
        }
        return size;
    }
    public void delete(String path) {
        String[] pathParts = path.split("/");
        deleteDFS(root, pathParts, 0);
    }

    private boolean deleteDFS(FileSystemNode node, String[] pathParts, int level) {
        if (node == null) {
            return false;
        }

        if (level == pathParts.length - 1) {
            if (node.children.containsKey(pathParts[level])) {
                node.children.remove(pathParts[level]);
                return true;
            }
            return false;
        }

        boolean deleted = deleteDFS(node.children.get(pathParts[level]), pathParts, level + 1);

        // Clean up the empty nodes during the backtracking phase
        if (deleted && node.children.get(pathParts[level]).children.isEmpty()) {
            node.children.remove(pathParts[level]);
        }

        return deleted;
    }

    public FileSystemNode search(String path) {
        String[] pathParts = path.split("/");
        FileSystemNode curr = root;
        for (String part : pathParts) {
            if (!curr.children.containsKey(part)) {
                return null;
            }
            curr = curr.children.get(part);
        }
        return curr;
    }
}

class FileSystemSnapshot {
    FileSystemTrie trie;

    public FileSystemSnapshot() {
        trie = new FileSystemTrie();
    }
}

public class FileSystemTool {
    FileSystemSnapshot snap1;
    FileSystemSnapshot snap2;

    public FileSystemTool() {
        snap1 = new FileSystemSnapshot();
        snap2 = new FileSystemSnapshot();
    }

    // Method to load items into snapshots
    public void loadItems(FileSystemSnapshot snap, Map<String, FileSystemItem> items) {
        for (Map.Entry<String, FileSystemItem> entry : items.entrySet()) {
            snap.trie.add(entry.getKey(), entry.getValue());
        }
    }

    // Method to add an item to the file system
    public void addItem(FileSystemSnapshot snap, String path, FileSystemItem item) {
        snap.trie.add(path, item);
    }

    // Method to delete an item from the file system
    public void deleteItem(FileSystemSnapshot snap, String path) {
        snap.trie.delete(path);
    }

    // Other methods to print items, find differences, etc.
    // ... (The previous methods: printAllItems, printNewItems, printDeletedItems, printModifiedItems, isPathPresent, dumpSizeUnderPath)
    public void printAllItems(FileSystemSnapshot snap) {
        System.out.println("Items in the snapshot:");
        printAllItemsDFS(snap.trie.root, "");
    }

    private void printAllItemsDFS(FileSystemNode node, String path) {
        if (node == null) {
            return;
        }

        if (node.item != null) {
            System.out.println(path + node.item.name);
        }

        for (String childName : node.children.keySet()) {
            FileSystemNode childNode = node.children.get(childName);
            printAllItemsDFS(childNode, path + childName + "/");
        }
    }

    // Method to print new items (items that are there only in Snap2 but not in Snap1)
    public void printNewItems() {
        System.out.println("New items in Snap2:");
        printNewItemsDFS(snap2.trie.root, snap1.trie.root, "");
    }

    private void printNewItemsDFS(FileSystemNode node2, FileSystemNode node1, String path) {
        if (node2 == null) {
            return;
        }

        if (node2.item != null && node1 == null) {
            System.out.println(path + node2.item.name);
        }

        for (String childName : node2.children.keySet()) {
            FileSystemNode childNode2 = node2.children.get(childName);
            FileSystemNode childNode1 = (node1 != null) ? node1.children.get(childName) : null;
            printNewItemsDFS(childNode2, childNode1, path + childName + "/");
        }
    }

    // Method to print deleted items (items that are there only in Snap1 but not in Snap2)
    public void printDeletedItems() {
        System.out.println("Deleted items in Snap2:");
        printDeletedItemsDFS(snap1.trie.root, snap2.trie.root, "");
    }

    private void printDeletedItemsDFS(FileSystemNode node1, FileSystemNode node2, String path) {
        if (node1 == null) {
            return;
        }

        if (node1.item != null && node2 == null) {
            System.out.println(path + node1.item.name);
        }

        for (String childName : node1.children.keySet()) {
            FileSystemNode childNode1 = node1.children.get(childName);
            FileSystemNode childNode2 = (node2 != null) ? node2.children.get(childName) : null;
            printDeletedItemsDFS(childNode1, childNode2, path + childName + "/");
        }
    }

    // Method to print modified items (items that are there in both snaps but have different mtime or size)
    public void printModifiedItems() {
        System.out.println("Modified items:");
        printModifiedItemsDFS(snap1.trie.root, snap2.trie.root, "");
    }

    private void printModifiedItemsDFS(FileSystemNode node1, FileSystemNode node2, String path) {
        if (node1 == null || node2 == null) {
            return;
        }

        if (node1.item != null && node2.item != null) {
            if (!node1.item.name.equals(node2.item.name) || node1.item.mtime != node2.item.mtime || node1.item.size != node2.item.size) {
                System.out.println(path + node2.item.name);
            }
        }

        for (String childName : node2.children.keySet()) {
            FileSystemNode childNode1 = node1.children.get(childName);
            FileSystemNode childNode2 = node2.children.get(childName);
            printModifiedItemsDFS(childNode1, childNode2, path + childName + "/");
        }
    }
   // Method to check if a path is present in Snap1/Snap2
   public boolean isPathPresent(FileSystemSnapshot snap, String path) {
    FileSystemNode node = snap.trie.search(path);
    return node != null;
}

    // Method to dump the entire file system tree
    public void dumpFileSystem(FileSystemSnapshot snap) {
        System.out.println("File System Tree:");
        dumpFileSystemDFS(snap.trie.root, "");
    }

    private void dumpFileSystemDFS(FileSystemNode node, String path) {
        if (node == null) {
            return;
        }

        if (node.item != null) {
            System.out.println(path + node.item.name);
        }

        for (String childName : node.children.keySet()) {
            FileSystemNode childNode = node.children.get(childName);
            dumpFileSystemDFS(childNode, path + childName + "/");
        }
    }
 // Method to dump the sum of the size of all items underneath a given path
 public long dumpSizeUnderPath(FileSystemSnapshot snap, String path) {
    FileSystemNode node = snap.trie.search(path);
    return (node != null) ? getSizeUnderNode(node) : 0;
}
private long getSizeUnderNode(FileSystemNode node) {
    if (node == null) {
        return 0;
    }

    long size = (node.item != null) ? node.item.size : 0;
    for (FileSystemNode child : node.children.values()) {
        size += getSizeUnderNode(child);
    }
    return size;
}

    public static void main(String[] args) {
        FileSystemTool fileSystemTool = new FileSystemTool();

        // Load items into snap1 and snap2 (For demonstration, we're using a small set of items)
        Map<String, FileSystemItem> items1 = new HashMap<>();
        items1.put("/folder1/file1.txt", new FileSystemItem("file1.txt", 1628112367L, 1024));
        items1.put("/folder1/file2.txt", new FileSystemItem("file2.txt", 1628112367L, 512));
        items1.put("/folder2/subfolder/file3.txt", new FileSystemItem("file3.txt", 1628112367L, 2048));
        fileSystemTool.loadItems(fileSystemTool.snap1, items1);

        Map<String, FileSystemItem> items2 = new HashMap<>();
        items2.put("/folder1/file1.txt", new FileSystemItem("file1.txt", 1631558367L, 1024)); // Modified
        items2.put("/folder1/file2.txt", new FileSystemItem("file2.txt", 1628112367L, 512));
        items2.put("/folder2/subfolder/file3.txt", new FileSystemItem("file3.txt", 1628112367L, 2048));
        items2.put("/folder3/file4.txt", new FileSystemItem("file4.txt", 1630752367L, 512)); // New
        fileSystemTool.loadItems(fileSystemTool.snap2, items2);

        // Ask the user for the operation to perform
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("\nChoose an operation:");
            System.out.println("1. Print all items in Snap1");
            System.out.println("2. Print all items in Snap2");
            System.out.println("3. Print new items in Snap2");
            System.out.println("4. Print deleted items in Snap2");
            System.out.println("5. Print modified items");
            System.out.println("6. Check if a path is present in Snap2");
            System.out.println("7. Dump size under a path in Snap2");
            System.out.println("8. Dump the entire file system tree");
            System.out.println("9. Add an item to the file system");
            System.out.println("10. Delete an item from the file system");
            System.out.println("0. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    fileSystemTool.printAllItems(fileSystemTool.snap1);
                    break;
                case 2:
                    fileSystemTool.printAllItems(fileSystemTool.snap2);
                    break;
                case 3:
                    fileSystemTool.printNewItems();
                    break;
                case 4:
                    fileSystemTool.printDeletedItems();
                    break;
                case 5:
                    fileSystemTool.printModifiedItems();
                    break;
                case 6:
                    System.out.print("Enter the path to check: ");
                    String path = scanner.nextLine();
                    boolean isPresent = fileSystemTool.isPathPresent(fileSystemTool.snap2, path);
                    System.out.println("Path is present in Snap2: " + isPresent);
                    break;
                case 7:
                    System.out.print("Enter the path to dump size: ");
                    String pathToDump = scanner.nextLine();
                    long size = fileSystemTool.dumpSizeUnderPath(fileSystemTool.snap2, pathToDump);
                    System.out.println("Size of items under path: " + size + " bytes");
                    break;
                case 8:
                    fileSystemTool.dumpFileSystem(fileSystemTool.snap2);
                    break;
                case 9:
                    System.out.print("Enter the path to add: ");
                    String pathToAdd = scanner.nextLine();
                    System.out.print("Enter the item name: ");
                    String itemName = scanner.nextLine();
                    System.out.print("Enter the item size: ");
                    long itemSize = scanner.nextLong();
                    System.out.print("Enter the item modification time: ");
                    long itemMtime = scanner.nextLong();
                    scanner.nextLine(); // Consume the newline character after reading the long inputs
                    fileSystemTool.addItem(fileSystemTool.snap2, pathToAdd + "/" + itemName,
                            new FileSystemItem(itemName, itemMtime, itemSize));
                    break;
                case 10:
                    System.out.print("Enter the path to delete: ");
                    String pathToDelete = scanner.nextLine();
                    fileSystemTool.deleteItem(fileSystemTool.snap2, pathToDelete);
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }
}
