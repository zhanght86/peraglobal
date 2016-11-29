package com.peraglobal.km.crawler.db.biz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CacheImp implements Cache {
	  private SortedMap<Object,List<Map<String,Object>>> theMap = null;
	  private boolean isOpen = false;
	  String primaryKeyName = null;
	@Override
	public void open(Context context) {
		  isOpen = true;
		    if (theMap == null) {
		      theMap = new TreeMap<Object,List<Map<String,Object>>>();
		    }
		    String pkName = CachePropertyUtil.getAttributeValueAsString(context,
		            CacheSupport.CACHE_PRIMARY_KEY);
		        if (pkName != null) {
		          primaryKeyName = pkName;
		        }
	}
	private void checkOpen(boolean shouldItBe) {
	    if (!isOpen && shouldItBe) {
	      throw new IllegalStateException(
	          "Must call open() before using this cache.");
	    }
	    if (isOpen && !shouldItBe) {
	      throw new IllegalStateException("The cache is already open.");
	    }
	  }

	@Override
	public void close() {
		   isOpen = false;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		deleteAll(true);
	    theMap = null;
	    isOpen = false;
	}

	private void deleteAll(boolean b) {
		  if (theMap != null) {
		      theMap.clear();
		    }
	}
	@Override
	public void add(Map<String, Object> rec) {
		checkOpen(true);
		 if (rec == null || rec.size() == 0) {
		      return;
		    }
		 if (primaryKeyName == null) {
		      primaryKeyName = rec.keySet().iterator().next();
		    }
		    
		    Object pk = rec.get(primaryKeyName);
		    if (pk instanceof Collection<?>) {
		      Collection<Object> c = (Collection<Object>) pk;
		      if (c.size() != 1) {
		        throw new RuntimeException(
		            "The primary key must have exactly 1 element.");
		      }
		      pk = c.iterator().next();
		    }
		    //Rows with null keys are not added.
		    if(pk==null) {
		      return;
		    }
		    List<Map<String,Object>> thisKeysRecs = theMap.get(pk);
		    if (thisKeysRecs == null) {
		      thisKeysRecs = new ArrayList<Map<String,Object>>();
		      theMap.put(pk, thisKeysRecs);
		    }
		    thisKeysRecs.add(rec);
	}

	@Override
	public Iterator<Map<String, Object>> iterator() {
		 return new Iterator<Map<String, Object>>() {
		        private Iterator<Map.Entry<Object,List<Map<String,Object>>>> theMapIter;
		        private List<Map<String,Object>> currentKeyResult = null;
		        private Iterator<Map<String,Object>> currentKeyResultIter = null;

		        {
		            theMapIter = theMap.entrySet().iterator();
		        }

		        @Override
		        public boolean hasNext() {
		          if (currentKeyResultIter != null) {
		            if (currentKeyResultIter.hasNext()) {
		              return true;
		            } else {
		              currentKeyResult = null;
		              currentKeyResultIter = null;
		            }
		          }

		          Map.Entry<Object,List<Map<String,Object>>> next = null;
		          if (theMapIter.hasNext()) {
		            next = theMapIter.next();
		            currentKeyResult = next.getValue();
		            currentKeyResultIter = currentKeyResult.iterator();
		            if (currentKeyResultIter.hasNext()) {
		              return true;
		            }
		          }
		          return false;
		        }

		        @Override
		        public Map<String,Object> next() {
		          if (currentKeyResultIter != null) {
		            if (currentKeyResultIter.hasNext()) {
		              return currentKeyResultIter.next();
		            } else {
		              currentKeyResult = null;
		              currentKeyResultIter = null;
		            }
		          }

		          Map.Entry<Object,List<Map<String,Object>>> next = null;
		          if (theMapIter.hasNext()) {
		            next = theMapIter.next();
		            currentKeyResult = next.getValue();
		            currentKeyResultIter = currentKeyResult.iterator();
		            if (currentKeyResultIter.hasNext()) {
		              return currentKeyResultIter.next();
		            }
		          }
		          return null;
		        }

		        @Override
		        public void remove() {
		          throw new UnsupportedOperationException();
		        }
		    };
	}

	@Override
	public Iterator<Map<String, Object>> iterator(Object key) {
		 checkOpen(true);
		    if(key==null) {
		      return null;
		    }
		    if(key instanceof Iterable<?>) {
		      List<Map<String,Object>> vals = new ArrayList<Map<String,Object>>();
		      Iterator<?> iter = ((Iterable<?>) key).iterator();
		      while(iter.hasNext()) {
		        List<Map<String,Object>> val = theMap.get(iter.next());
		        if(val!=null) {
		          vals.addAll(val);
		        }
		      } 
		      if(vals.size()==0) {
		        return null;
		      }
		      return vals.iterator();
		    }    
		    List<Map<String,Object>> val = theMap.get(key);
		    if (val == null) {
		      return null;
		    }
		    return val.iterator();
	}

	@Override
	public void delete(Object key) {
		 checkOpen(true);
		    if(key==null) {
		      return;
		    }
		    theMap.remove(key);
	}

	@Override
	public void deleteAll() {
		deleteAll(false);
	}

}
