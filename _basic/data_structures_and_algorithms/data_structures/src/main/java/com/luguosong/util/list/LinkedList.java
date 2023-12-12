package com.luguosong.util.list;

/**
 * 链表实现
 *
 * @author luguosong
 */
public class LinkedList<E> extends AbstractList<E> {


    private Node<E> first;


    /*
     * 定义节点类
     * */
    private static class Node<E> {
        E item;
        Node<E> next;

        public Node(E element, Node<E> next) {
            this.item = element;
            this.next = next;
        }
    }


    /**
     * 清空元素
     * <p>
     * 说明：当第一个元素为null，其中的next元素也会为null，next元素将不再被引用，会被垃圾回收器回收。
     * 以此类推，所有元素都会被回收。
     */
    @Override
    public void clear() {
        size = 0;
        first = null;
    }

    /**
     * 获取元素
     *
     * @param index 要返回元素的索引
     * @return
     */
    @Override
    public E get(int index) {
        return node(index).item;
    }

    /**
     * 设置元素
     *
     * @param index   要替换元素的索引
     * @param element 元素将被存储在指定位置
     * @return 返回原来的元素
     */
    @Override
    public E set(int index, E element) {
        //获取index位置的元素
        Node<E> node = node(index);
        //获取原来的元素
        E old = node.item;
        //将index位置的元素替换为新元素
        node.item = element;
        //返回原来的元素
        return old;
    }

    /**
     * 添加元素
     *
     * @param index   插入指定元素的索引
     * @param element 要插入的元素
     */
    @Override
    public void add(int index, E element) {
        if (index == 0) {
            //index没有前一项，需要特殊处理
            first = new Node<>(element, first);
        } else {
            //node(index - 1).next = new Node<>(element, node(index));

            //获取index位置的前一个节点
            Node<E> preNode = node(index - 1);
            //将新节点插入到前一个节点的后面
            //preNode.next = new Node<>(element, node(index));
            preNode.next = new Node<>(element, preNode.next);
        }
        size++;
    }

    /**
     * 删除元素
     *
     * @param index 要删除的元素的索引
     * @return 返回被删除的元素
     */
    @Override
    public E remove(int index) {
        E old = null;
        if (index == 0) {
            // index没有前一项，需要特殊处理
            // 获取第一个元素
            old = first.item;
            // 将first设置为第二个节点
            first = first.next;
        } else {
            // 获取index位置的前一个节点
            Node<E> preNode = node(index - 1);
            // 获取要删除的节点元素
            old = preNode.next.item;
            // 将前一个节点的next指向要删除节点的next
            preNode.next = preNode.next.next;
        }
        // 元素数量-1
        size--;
        // 返回被删除的元素
        return old;
    }

    /**
     * 查询元素位置
     *
     * @param o 要搜索的元素
     * @return 返回元素的索引
     */
    @Override
    public int indexOf(Object o) {

        if (o == null) {
            //元素为null
            Node<E> node = first;
            for (int i = 0; i < size; i++) {
                if (node.item == null) return i;
                node = node.next;
            }
        } else {
            //元素不为null
            Node<E> node = first;
            for (int i = 0; i < size; i++) {
                if (o.equals(node.item)) return i;
                node = node.next;
            }
        }
        // 没有找到
        return -1;
    }

    /**
     * 打印链表
     *
     * @return 返回元素的字符串
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("[");
        Node<E> node = first;
        for (int i = 0; i < size; i++) {
            ret.append(node.item);
            if (i != size - 1) {
                ret.append(",");
            }
            node = node.next;
        }
        ret.append("]");
        return ret.toString();
    }

    /**
     * 工具方法：返回索引对应的节点
     *
     * @param index 索引
     * @return 索引对应的节点
     */
    private Node<E> node(int index) {
        //检查index范围
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index:" + index + ",Size:" + size);

        Node<E> node = first;
        for (int i = 0; i < index; i++) {
            node = node.next;
        }
        return node;
    }
}
