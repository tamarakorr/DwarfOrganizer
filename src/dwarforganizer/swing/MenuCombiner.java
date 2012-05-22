/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import dwarforganizer.deepclone.DeepCloneUtils;
import dwarforganizer.deepclone.DeepCloneable;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Intended to smooth the process of combining the JMenus of a parent frame
 * with a child frame. Uses a priority queue to sort the menus.
 * From Java 1.5 PriorityQueue documentation:
 *       The Iterator provided in method iterator() is not guaranteed to
 *       traverse the elements of the PriorityQueue in any particular order.
 *       If you need ordered traversal, consider using
 *       Arrays.sort(pq.toArray()).
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class MenuCombiner {

    private PriorityQueue<MenuQueue> mqMainQueue;

    // Comparators are created by init()
    private Comparator MENU_COMPARATOR; // For ordering JMenus
    private Comparator ITEM_COMPARATOR; // For ordering JMenuItems
    private Comparator COMBINE_COMPARATOR; // For combining JMenus of same name

    // Creates the main queue based on the given collection of MenuQueues
    public MenuCombiner(final Collection<MenuQueue> mainList) {
        super();
        init();
        this.mqMainQueue = createQueueFromCollections(mainList
                , MENU_COMPARATOR);
    }
    // Creates the main queue based on the given JMenuBar, desired ordering of
    // JMenus, and desired ordering of JMenuItems (indices match).
    public MenuCombiner(final JMenuBar menuBar, final int[] menuOrder
            , final ArrayList<Integer>[] menuItemOrder) {

        super();
        init();
        this.mqMainQueue = createQueueFromMenuBar(menuBar, menuOrder
                , menuItemOrder);
    }
    // Creates the main queue from the given array of JMenus, desired ordering
    // of those JMenus, and desired ordering of their JMenuItems (indices
    // match).
    public MenuCombiner(final JMenu[] menus, final int[] menuOrder
            , final ArrayList<Integer>[] menuItemOrder) {
        super();
        init();
        this.mqMainQueue = createQueueFromMenus(menus, menuOrder
                , menuItemOrder);
    }
    public MenuCombiner(final MenuInfo menuInfo) {
        super();
        init();
        this.mqMainQueue = createQueueFromMenuBar(menuInfo.getMenuBar()
                , menuInfo.getMenuPriority(), menuInfo.getMenuItemPriority());
    }
    // Returns an internal frame listener for a child frame, which calls
    // combine(childQueue) when activated and separate() when deactivated.
    // Given: the desired window for the resulting JMenuBar; an array of child
    // JMenus, the desired ordering for those JMenus; and the desired ordering
    // for their JMenuItems.
    // NOTE: Only works for menu count > 0
    public InternalFrameListener createInternalFrameListener(
            final JFrame mainWindow, final JMenu[] menus, final int[] ordering
            , final ArrayList<Integer>[] menuItemOrder) {

        final PriorityQueue<MenuQueue> childQueue = createQueueFromMenus(menus
                , ordering, menuItemOrder);
        return getInternalFrameAdapter(mainWindow, childQueue);
    }
    // Returns an internal frame listener for a child frame, which calls
    // combine(childQueue) when activated and separate() when deactivated.
    // Given: the desired window for the resulting JMenuBar; the child JMenuBar;
    // the desired ordering for the child JMenus; and the desired ordering
    // for their JMenuItems.
    // NOTE: Only works for menu count > 0
    public InternalFrameListener createInternalFrameListener(
            final JFrame mainWindow, final MenuInfo menuInfo) {

        final PriorityQueue<MenuQueue> childQueue = createQueueFromMenuBar(
                menuInfo.getMenuBar(), menuInfo.getMenuPriority()
                , menuInfo.getMenuItemPriority());
        return getInternalFrameAdapter(mainWindow, childQueue);
    }
    // Combines the given collection of menus with the set of main menus (main
    // menus are set by constructor), and returns the resulting JMenuBar.
    public JMenuBar combine(final Collection<MenuQueue> childList) {
        PriorityQueue<MenuQueue> queue = createQueueFromCollections(mqMainQueue
                , childList);
        final Comparator<CombinableMenu> comp = COMBINE_COMPARATOR;
        queue = combineEqualEntries(queue, comp);
        return createBarFromQueue(queue);
    }
    // Returns the JMenuBar representing only the main menus (originally set by
    // constructor).
    public JMenuBar separate() {
        return createBarFromQueue(DeepCloneUtils.deepClone(mqMainQueue));
    }
    public static class MenuInfo {
        private JMenuBar menuBar;
        private int[] menuPriority;
        private ArrayList<Integer>[] menuItemPriority;

        public MenuInfo(final JMenuBar menuBar, final int[] menuPriority
                , final ArrayList<Integer>[] menuItemPriority) {
            this.menuBar = menuBar;
            this.menuPriority = menuPriority;
            this.menuItemPriority = menuItemPriority;
        }
        public JMenuBar getMenuBar() {
            return menuBar;
        }
        public ArrayList<Integer>[] getMenuItemPriority() {
            return menuItemPriority;
        }
        public int[] getMenuPriority() {
            return menuPriority;
        }
    }
    // Represents an object with a priority (such as for priority queueing):
    public interface Ordered {
        public int getOrder();
    }
    // Represents a JMenu with a priority order:
    public class CombinableMenu implements Ordered {
        private JMenu menu;
        private int order;
        public CombinableMenu(final JMenu menu, final int order) {
            this.menu = menu;
            this.order = order;
        }
        @Override
        public int getOrder() {
            return order;
        }
        public JMenu getMenu() {
            return menu;
        }
    }
    // Represents a JMenuItem with a priority order:
    public class CombinableMenuItem implements Ordered
            , DeepCloneable<CombinableMenuItem> {

        private Component item;
        private int order;
        public CombinableMenuItem(final Component item, final int order) {
            this.item = item;
            this.order = order;
        }
        @Override
        public CombinableMenuItem deepClone() {
            return new CombinableMenuItem(item, order);
        }
        @Override
        public int getOrder() {
            return order;
        }
        public Component getMenuItem() {
            return item;
        }
    }
    // MenuQueue represents a JMenu with a priority order and a priority queue
    // of menu items.
    public class MenuQueue extends CombinableMenu
            implements DeepCloneable<MenuQueue> {

        private PriorityQueue<CombinableMenuItem> queue;

        public MenuQueue(final JMenu menu, final int order
                , final PriorityQueue<CombinableMenuItem> queue) {
            super(menu, order);
            this.queue = queue;
        }
        public PriorityQueue<CombinableMenuItem> getQueue() {
            return queue;
        }

        @Override
        public MenuQueue deepClone() {
            return new MenuQueue(getMenu(), getOrder()
                    , DeepCloneUtils.deepClone(queue));
        }
    }
    // Returns an internal frame adapter to automatically combine and separate
    // menus, for the given menu bar window and the given child window menu
    // queue.
    private InternalFrameAdapter getInternalFrameAdapter(final JFrame mainWindow
            , final PriorityQueue<MenuQueue> childQueue) {
        return new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(final InternalFrameEvent e) {
                mainWindow.setJMenuBar(combine(childQueue));
            }
            @Override
            public void internalFrameDeactivated(final InternalFrameEvent e) {
                mainWindow.setJMenuBar(separate());
            }
        };
    }
    // Comparator for sorting JMenus
    private class CombinableMenuOrderComparator
            implements Comparator<CombinableMenu> {
        @Override
        public int compare(final CombinableMenu o1, final CombinableMenu o2) {
            return basicCompare(o1, o2);
        }
    }
    // Comparator for combining JMenus with the same text
    private class CombinableMenuCombineComparator
            implements Comparator<CombinableMenu> {

        @Override
        public int compare(final CombinableMenu o1, final CombinableMenu o2) {
            final String text1 = o1.getMenu().getText();
            final String text2 = o2.getMenu().getText();
            if (text1.equals(text2)) {
                return 0;
            }
            else {
                return basicCompare(o1, o2);
            }
        }
    }
    // Comparator for sorting JMenuItems
    private class CombinableMenuItemComparator
            implements Comparator<CombinableMenuItem> {
        @Override
        public int compare(final CombinableMenuItem o1
                , final CombinableMenuItem o2) {

            return basicCompare(o1, o2);
        }
    }
    // Compares items based on Order property
    private int basicCompare(final Ordered o1, final Ordered o2) {
        final int order1 = o1.getOrder();
        final int order2 = o2.getOrder();
        final int iReturn;

        if (order1 < order2) {
            iReturn = -1;
        }
        else if (order1 == order2) {
            iReturn = 0;
        }
        else {
            iReturn = 1;
        }
        return iReturn;
    }
    // Constructor shared functionality
    private void init() {
        // Create comparators
        this.MENU_COMPARATOR = new CombinableMenuOrderComparator();
        this.ITEM_COMPARATOR = new CombinableMenuItemComparator();
        this.COMBINE_COMPARATOR = new CombinableMenuCombineComparator();
    }
    // Constructs and returns a JMenuBar based on the given queue. The
    // given queue is considered destroyed.
    private JMenuBar createBarFromQueue(final PriorityQueue<MenuQueue> queue) {

        final JMenuBar menuBar = new JMenuBar();

        // Disassemble the queue to get items in order
        while (queue.size() > 0) {
            final MenuQueue menuQueue = queue.remove();
            final JMenu menu = menuQueue.getMenu();
            menuBar.add(menu);

            // Remove and re-add all menu items
            menu.removeAll();

            // Add all
            final PriorityQueue<CombinableMenuItem> itemQueue
                    = menuQueue.getQueue();
            while (itemQueue.size() > 0) {
                final CombinableMenuItem cMenuItem = itemQueue.remove();
                final Component menuItem = cMenuItem.getMenuItem();
                menu.add(menuItem);
            }
        }
        return menuBar;
    }
    // Creates a new queue from the given main list and comparator,
    // and any optional number of other lists.
    private <T extends DeepCloneable> PriorityQueue<T>
            createQueueFromCollections(final Collection<T> mainList
            , final Comparator comparator, final Collection<T>... otherLists) {

        final PriorityQueue<T> qReturn;

        int size = mainList.size();
        for (final Collection<T> list : otherLists) {
            size += list.size();
        }

        qReturn = new PriorityQueue<T>(size, comparator);
        qReturn.addAll(DeepCloneUtils.deepClone(mainList));
        deepCloneAndEnqueueItems(qReturn, otherLists);

        return qReturn;
    }
    // Deep clones a priority queue from the given priority queue and
    // any given collections, and returns it.
    private <T extends DeepCloneable> PriorityQueue<T>
            createQueueFromCollections(final PriorityQueue<T> mainQueue
            , final Collection<T>... otherLists) {

        final PriorityQueue<T> qReturn = DeepCloneUtils.deepClone(mainQueue);
        deepCloneAndEnqueueItems(qReturn, otherLists);

        return qReturn;
    }
    // Deep clones the items in all the collections and adds them to the
    // given priority queue.
    private <T extends DeepCloneable> void deepCloneAndEnqueueItems(
            final PriorityQueue<T> queue, final Collection<T>... collections) {
        for (final Collection<T> collection : collections) {
            queue.addAll(DeepCloneUtils.deepClone(collection));
        }
    }
    // Prints the given priority MenuQueue.
    private void printQueue(final PriorityQueue<MenuQueue> queue) {
        final String INDENT = "  ";

        System.out.println("Printing priority queue:");
        for (final MenuQueue mq : queue) {
            System.out.println(mq.getOrder() + ": "
                    + mq.getMenu().getText());
            for (final CombinableMenuItem item : mq.getQueue()) {
                final Component comp = item.getMenuItem();
                if (comp instanceof JMenuItem) {
                    final JMenuItem menuItem = (JMenuItem) comp;
                    System.out.println(INDENT + menuItem.getText());
                }
                else if (comp instanceof JSeparator) {
                    System.out.println(INDENT + "[JSeparator]");
                }
                else
                    System.out.println(INDENT + "[UNKNOWN COMPONENT TYPE]");
            }
        }
        System.out.println("-------------");
    }
    // Creates and returns a priority MenuQueue from the given JMenuBar,
    // desired menu ordering, and desired JMenuItem ordering.
    private PriorityQueue<MenuQueue> createQueueFromMenuBar(
            final JMenuBar menuBar, final int[] menuOrder
            , final ArrayList<Integer>[] menuItemOrder) {

        final JMenu[] menus = menuBarToMenuArray(menuBar);
        return createQueueFromMenus(menus, menuOrder, menuItemOrder);
    }
    // Converts JMenuBar to JMenu[]
    private JMenu[] menuBarToMenuArray(final JMenuBar menuBar) {
        final int numMenus = menuBar.getMenuCount();
        final JMenu[] menus = new JMenu[numMenus];

        for (int iCount = 0; iCount < numMenus; iCount++) {
            final JMenu menu = menuBar.getMenu(iCount);
            menus[iCount] = menu;
        }
        return menus;
    }
    // Creates and returns a priority MenuQueue from the given array of JMenus,
    // their desired ordering, desired ordering of JMenuItems.
    private PriorityQueue<MenuQueue> createQueueFromMenus(final JMenu[] menus
            , final int[] ordering
            , final ArrayList<Integer>[] menuItemOrder) {

        final PriorityQueue<MenuQueue> qReturn = new PriorityQueue<MenuQueue>(
                menus.length, MENU_COMPARATOR);
        if (! validateLengths(ordering.length, menus.length)) {
            return qReturn;
        }

        for (int iCount = 0; iCount < menus.length; iCount++) {
            final PriorityQueue<CombinableMenuItem> itemQueue
                    = createMenuItemQueue(menus[iCount]
                    , menuItemOrder[iCount]);
            final MenuQueue mq = new MenuQueue(menus[iCount], ordering[iCount]
                    , itemQueue);
            qReturn.add(mq);
        }
        return qReturn;
    }
    // Returns false if the given values are invalid; otherwise returns true.
    private boolean validateLengths(final int orderingLength
            , final int menusLength) {

        if (orderingLength != menusLength) {
            System.err.println("The ordering array must have the same number of"
                    + " elements as the menu count (" + orderingLength + "!="
                    + menusLength);
            return false;
        }
        return true;
    }
    // Returns a CombinableMenuItem queue created from the given JMenu and
    // desired ordering for JMenuItems.
    private PriorityQueue<CombinableMenuItem> createMenuItemQueue(
            final JMenu menu, final ArrayList<Integer> lstOrder) {

        final int numItems = menu.getMenuComponentCount();
        final PriorityQueue<CombinableMenuItem> itemQueue
                = new PriorityQueue<CombinableMenuItem>(numItems
                , ITEM_COMPARATOR);
        for (int jCount = 0; jCount < numItems; jCount++) {
            final Component comp = menu.getMenuComponent(jCount);
            //if (comp instanceof JMenuItem)
            itemQueue.add(new CombinableMenuItem(comp, lstOrder.get(jCount))); //(JMenuItem)
        }
        return itemQueue;
    }
    // Combines entries in the given queue based on the given combineComparator,
    // and clones all resulting items, returning a cloned queue.
    private PriorityQueue<MenuQueue> combineEqualEntries(
            final PriorityQueue<MenuQueue> queue
            , final Comparator<CombinableMenu> combineComparator) {

        boolean bCombined;
        final int size = queue.size();
        final PriorityQueue<MenuQueue> qReturn;

        // Use an ArrayList so that we can traverse the list item-by-item,
        // checking for equality with combineComparator
        final ArrayList<MenuQueue> aHelper = new ArrayList<MenuQueue>(size);

        for (final MenuQueue mq : queue) {
            bCombined = false;
            for (int iCount = 0; iCount < aHelper.size(); iCount++) {
                final MenuQueue listItem = aHelper.get(iCount);
                if (0 == combineComparator.compare(mq, listItem)) {
                    listItem.getQueue().addAll(mq.getQueue());
                    bCombined = true;
                    break;
                }
            }
            if (! bCombined) {
                aHelper.add(mq);
            }
        }
        qReturn = createQueueFromCollections(aHelper, MENU_COMPARATOR);
        return qReturn;
    }
}