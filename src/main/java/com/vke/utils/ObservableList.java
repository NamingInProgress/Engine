package com.vke.utils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObservableList<L extends List<T>, T> extends AbstractList<T> {
    private final L list;
    private final ArrayList<Observer> observers;

    public ObservableList(L baseList) {
        this.list = baseList;
        this.observers = new ArrayList<>();
    }

    public void observe(Observer observer) {
        observers.add(observer);
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    private void change() {
        observers.forEach(Observer::changed);
    }

    //--------------------

    @Override
    public boolean add(T t) {
        change();
        return super.add(t);
    }

    @Override
    public T set(int index, T element) {
        change();
        return super.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        change();
        super.add(index, element);
    }

    @Override
    public T remove(int index) {
        change();
        return super.remove(index);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        change();
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean remove(Object o) {
        change();
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        change();
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        change();
        return super.retainAll(c);
    }

    public interface Observer {
        void changed();
    }
}
