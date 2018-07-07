package com.github.anrimian.simplemusicplayer.data.utils.folders;

import com.github.anrimian.simplemusicplayer.data.repositories.music.folders.CompositionNode;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.utils.Lists.mapList;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.*;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.ADDED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.DELETED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.MODIFY;
import static io.reactivex.Observable.fromArray;
import static io.reactivex.subjects.PublishSubject.create;
import static java.util.Collections.singletonList;

public class RxNode<K> {

    private final PublishSubject<Change<List<RxNode<K>>>> childChangeSubject = create();
    private final PublishSubject<Change<NodeData>> selfChangeSubject = create();

    private final LinkedHashMap<K, RxNode<K>> nodes = new LinkedHashMap<>();

    private K key;
    private NodeData data;

    @Nullable
    private RxNode<K> parent;

    public RxNode(K key, NodeData data) {
        this.key = key;
        this.data = data;
    }

    @Nullable
    public RxNode<K> getParent() {
        return parent;
    }

    @Nonnull
    public List<RxNode<K>> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    @Nonnull
    public K getKey() {
        return key;
    }

    public NodeData getData() {
        return data;
    }

    public Observable<Change<List<RxNode<K>>>> getChildChangeObservable() {
        return childChangeSubject;
    }

    public PublishSubject<Change<NodeData>> getSelfChangeObservable() {
        return selfChangeSubject;
    }

    public void addNodes(List<RxNode<K>> newNodes) {
        List<RxNode<K>> addedNodes = new ArrayList<>();
        List<RxNode<K>> modifiedNodes = new ArrayList<>();
        for (RxNode<K> newNode : newNodes) {
            newNode.parent = this;
            RxNode<K> previous = nodes.put(newNode.getKey(), newNode);
            if (previous == null) {
                addedNodes.add(newNode);
            } else {
                modifiedNodes.add(newNode);
            }
        }
        if (!addedNodes.isEmpty()) {
            childChangeSubject.onNext(new Change<>(ADDED, addedNodes));
            notifyNodesAdded(mapList(addedNodes, new ArrayList<>(), RxNode::getData));
        }
        if (!modifiedNodes.isEmpty()) {
            childChangeSubject.onNext(new Change<>(MODIFY, modifiedNodes));
        }
    }

    public void addNode(RxNode<K> node) {
        node.parent = this;
        RxNode<K> previous = nodes.put(node.getKey(), node);
        if (previous == null) {
            childChangeSubject.onNext(new Change<>(ADDED, singletonList(node)));
        } else {
            childChangeSubject.onNext(new Change<>(MODIFY, singletonList(node)));
        }
        notifyNodesAdded(singletonList(node.getData()));
    }

    public void removeNodes(List<K> keys) {
        List<RxNode<K>> removedNodes = new ArrayList<>();
        for (K key: keys) {
            RxNode<K> removedNode = nodes.remove(key);
            if (removedNode != null) {
                removedNodes.add(removedNode);
            }
        }
        if (!removedNodes.isEmpty()) {
            for (RxNode<K> removedNode: removedNodes){
                removedNode.notifySelfRemoved();
            }
            childChangeSubject.onNext(new Change<>(DELETED, removedNodes));
            notifyNodesRemoved(mapList(removedNodes, RxNode::getData));
        }
    }

    public void removeNode(K key) {
        RxNode<K> removedNode = nodes.remove(key);
        if (removedNode != null) {
            removedNode.notifySelfRemoved();
            childChangeSubject.onNext(new Change<>(DELETED, singletonList(removedNode)));
            notifyNodesRemoved(singletonList(removedNode.getData()));
        }
    }

    @Nullable
    public RxNode<K> getChild(K key) {
        return nodes.get(key);
    }

    public void updateNode(K key, NodeData nodeData) {
        RxNode<K> node = nodes.get(key);
        if (node != null) {
            node.data = nodeData;
            childChangeSubject.onNext(new Change<>(MODIFY, singletonList(node)));
        } else {
            addNode(new RxNode<>(key, nodeData));
        }
    }

    private void notifySelfRemoved() {
        selfChangeSubject.onNext(new Change<>(DELETED, data));
    }

    private void notifyNodesRemoved(List<NodeData> data) {
        if (this.data != null) {
            boolean updated = this.data.onNodesRemoved(data);
            if (updated) {
                selfChangeSubject.onNext(new Change<>(MODIFY, this.data));
            }
        }

        RxNode<K> parent = getParent();
        if (parent != null) {
            parent.notifyNodesRemoved(data);
        }
    }

    private void notifyNodesAdded(List<NodeData> data) {
        if (this.data != null) {
            boolean updated = this.data.onNodesAdded(data);
            if (updated) {
                selfChangeSubject.onNext(new Change<>(MODIFY, this.data));
            }
        }

        RxNode<K> parent = getParent();
        if (parent != null) {
            parent.notifyNodesAdded(data);
        }
    }

    @Override
    public String toString() {
        return "RxNode{" +
                "key=" + key +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RxNode<?> rxNode = (RxNode<?>) o;

        return key.equals(rxNode.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
