package no.geosoft.common.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A copy on write hash map.
 * <p>
 * A thread-safe variant of HashMap in which all mutative operations
 * (add, set, and so on) are implemented by making a fresh copy of the
 * underlying array.
 * <p>
 * Implementation based on CopyOnWriteArrayList.
 *
 * @param <K>  Key class.
 * @param <V>  Value class.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class CopyOnWriteHashMap<K,V> extends AbstractMap<K,V>
{
  /** Back-end map. */
  private volatile Map<K,V> map_ = Collections.<K,V>emptyMap();

  /**
   * Create a new empty copy-on-write hash map.
   */
  public CopyOnWriteHashMap()
  {
    // Nothing
  }

  /**
   * Create a new copy-on-write hash map and populate it with
   * the content of the specified map.
   *
   * @param map  Map to populate this map with. Non-null.
   * @throws IllegalArgumentException  If map is null.
   */
  public CopyOnWriteHashMap(Map<K,V> map)
  {
    super.putAll(map);
  }

  /** {@inheritDoc} */
  @Override
  public V get(Object key)
  {
    return map_.get(key);
  }

  /** {@inheritDoc} */
  @Override
  public synchronized V put(K key, V value)
  {
    HashMap<K, V> tmp = new HashMap<>(map_);
    V result = tmp.put(key, value);
    map_ = Collections.unmodifiableMap(tmp);
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized void putAll(Map<? extends K, ? extends V> m)
  {
    HashMap<K, V> tmp = new HashMap<>(map_);
    tmp.putAll(m);
    map_ = Collections.unmodifiableMap(tmp);
  }

  /** {@inheritDoc} */
  @Override
  public synchronized V remove(Object key)
  {
    HashMap<K, V> tmp = new HashMap<>(map_);
    V result = tmp.remove(key);
    map_ = Collections.unmodifiableMap(tmp);
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized void clear()
  {
    map_ = Collections.emptyMap();
  }

  /** {@inheritDoc} */
  @Override
  public Set<java.util.Map.Entry<K, V>> entrySet()
  {
    return map_.entrySet();
  }

  /** {@inheritDoc} */
  @Override
  public int size()
  {
    return map_.size();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty()
  {
    return map_.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsValue(Object value)
  {
    return map_.containsValue(value);
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(Object key)
  {
    return map_.containsKey(key);
  }

  /** {@inheritDoc} */
  @Override
  public Set<K> keySet()
  {
    return map_.keySet();
  }

  /** {@inheritDoc} */
  @Override
  public Collection<V> values()
  {
    return map_.values();
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o)
  {
    return map_.equals(o);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return map_.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return map_.toString();
  }

  /** {@inheritDoc} */
  @Override
  protected Object clone() throws CloneNotSupportedException
  {
    CopyOnWriteHashMap<K, V>clone = new CopyOnWriteHashMap<>();
    clone.map_ = map_;
    return clone;
  }
}
