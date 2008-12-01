package play.data.binding;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Unbinder {

	public static void unBind (Map<String, Object> result, Object src, String name) {
		unBind(result, src, src.getClass(),name);
	}
	
	private static void unBind (Map<String, Object> result, Object src, Class srcClazz, String name) {
		if (isDirect(srcClazz)) {
			if (!result.containsKey(name))
				result.put(name, src!=null ? src.toString() : null);
			else {
				List<Object> objects = (List<Object>)result.get(name);
				objects.add(src!=null ? src.toString() : null);
			}
		} else if (src.getClass().isArray()){
			List<Object> objects = new ArrayList<Object>();
			result.put(name, objects);
			Class clazz = src.getClass().getComponentType();
			int size = Array.getLength(src);
			for (int i=0;i<size;i++)
				unBind(result, Array.get(src, i), clazz, name);
		} else if (Collection.class.isAssignableFrom(src.getClass())) {
			if (Map.class.isAssignableFrom(src.getClass())) {
				throw new UnsupportedOperationException ("Unbind won't work with maps yet");
			} else {
				Collection c = (Collection) src;
				List<Object> objects = new ArrayList<Object>();
				result.put(name, objects);
				for (Object object : c) {
					unBind(result,object, object.getClass(), name);
				}
			}
		} else {
			Field[] fields = src.getClass().getDeclaredFields();
			for (Field field : fields) {
				String newName = name+"."+field.getName();
				boolean oldAcc = field.isAccessible();
				field.setAccessible(true);
				try {
					unBind(result,field.get(src), field.getType(), newName);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Object"+src.getClass()+" won't unbind field "+field.getName(),e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("Object"+src.getClass()+" won't unbind field "+field.getName(),e);
				}
				field.setAccessible(oldAcc);
			}
		}
	}
		
    public static boolean isDirect(Class clazz) {
    	return clazz.equals(String.class) 
    	|| clazz.equals(Integer.class)
    	|| clazz.equals(Boolean.class) 
    	|| clazz.equals(Long.class) 
    	|| clazz.equals(Double.class)
    	|| clazz.equals(Float.class)
    	|| clazz.equals(Short.class)
    	|| clazz.isPrimitive();
    }
	
}
