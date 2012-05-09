/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Intended for combining the JMenus of a parent frame with a child frame,
 * using a priority queue to sort the menus.
 *
 *     From oracle java 1.5 documentation about PriorityQueue:
         The Iterator provided in method iterator() is not guaranteed to
         traverse the elements of the PriorityQueue in any particular order.
         If you need ordered traversal, consider using
         Arrays.sort(pq.toArray()).

 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class MenuCombiner {

    private PriorityQueue<CombinableMenu> mqMainQueue;
    private Comparator moComparator;

    public MenuCombiner(Collection<CombinableMenu> mainList) {
        super();
        this.moComparator = new CombinableMenuComparator();
        this.mqMainQueue = createNewQueueFrom(mainList, moComparator);
    }
    public MenuCombiner(JMenuBar menuBar, int[] menuOrder) {
        super();
        this.moComparator = new CombinableMenuComparator();
        this.mqMainQueue = createNewQueueFrom(createListFromMenuBar(menuBar
                , menuOrder), moComparator);
    }
    public MenuCombiner(JMenu[] menus, int[] menuOrder) {
        super();
        this.moComparator = new CombinableMenuComparator();
        this.mqMainQueue = createNewQueueFrom(createListFromMenus(menus
                , menuOrder), moComparator);
    }
    private class CombinableMenuComparator
            implements Comparator<CombinableMenu> {

        @Override
        public int compare(CombinableMenu o1, CombinableMenu o2) {
            int order1 = o1.getOrder();
            int order2 = o2.getOrder();
            int iReturn;

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
    }
    public JMenuBar combine(Collection<CombinableMenu> childList) {
        return createBarFromQueue(createNewQueueFrom(mqMainQueue, moComparator
                , childList));
    }
    private JMenuBar createBarFromQueue(PriorityQueue<CombinableMenu> queue) {
        JMenuBar menuBar = new JMenuBar();
        // Disassemble the queue to get items in order
        while (queue.size() > 0) {
            CombinableMenu cMenu = queue.remove();
            menuBar.add(cMenu.getMenu());
        }
        return menuBar;
    }
    // Creates a new queue from the given main list and comparator,
    // and any optional number of other lists.
    private PriorityQueue<CombinableMenu> createNewQueueFrom(
            Collection<CombinableMenu> mainList, Comparator comparator
            , Collection<CombinableMenu>... otherLists) {

        PriorityQueue<CombinableMenu> qReturn;

        int size = mainList.size();
        for (Collection<CombinableMenu> list : otherLists) {
            size += list.size();
        }

        qReturn = new PriorityQueue<CombinableMenu>(size, comparator);
        qReturn.addAll(mainList);
        for (Collection<CombinableMenu> list : otherLists) {
            qReturn.addAll(list);
        }

        return qReturn;
    }
    public JMenuBar separate() {
        return createBarFromQueue(createNewQueueFrom(mqMainQueue
                , moComparator));
    }
    private void printQueue(PriorityQueue<CombinableMenu> queue) {
        System.out.println("Printing priority queue:");
        for (CombinableMenu menu : queue) {
            System.out.println(menu.getOrder() + ": " + menu.getMenu().getText());
        }
        System.out.println("-------------");
    }
    private ArrayList<CombinableMenu> createListFromMenuBar(
            JMenuBar menuBar, int[] ordering) {

        JMenu[] menus = new JMenu[menuBar.getMenuCount()];
        for (int iCount = 0; iCount < menuBar.getMenuCount(); iCount++) {
            menus[iCount] = menuBar.getMenu(iCount);
        }
        return createListFromMenus(menus, ordering);
    }
    private ArrayList<CombinableMenu> createListFromMenus(JMenu[] menus
            , int[] ordering) {
        ArrayList<CombinableMenu> list = new ArrayList<CombinableMenu>(
                menus.length);
        if (ordering.length != menus.length) {
            System.err.println("The ordering array must have the same number of"
                    + " elements as the menu count (" + ordering.length + "!="
                    + menus.length);
            return list;
        }

        for (int iCount = 0; iCount < menus.length; iCount++) {
            list.add(new CombinableMenu(menus[iCount], ordering[iCount]));
        }
        return list;
    }
    public InternalFrameListener createInternalFrameListener(
            final JFrame mainWindow, final JMenu[] menus
            , final int[] ordering) {

        final ArrayList<CombinableMenu> list = createListFromMenus(menus
                , ordering);

        return new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                mainWindow.setJMenuBar(combine(list));
            }
            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                mainWindow.setJMenuBar(separate());
            }
        };
    }
}
