package io.katharsis.jpa.internal.query;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.query.ComputedAttributeRegistry;
import io.katharsis.jpa.query.JpaQueryFactoryContext;

public class ComputedAttributeRegistryImpl implements ComputedAttributeRegistry {

	private Map<String, Registration> map = new HashMap<>();

	private JpaQueryFactoryContext context;

	private class Registration {

		private MetaComputedAttribute attr;

		private Object expressionFactory;

		private Class<?> targetClass;

		private Type type;

		private String name;

		public MetaComputedAttribute getAttribute() {
			if (attr == null) {
				synchronized (this) {
					if (attr == null) {
						MetaLookup metaLookup = context.getMetaLookup();
						MetaDataObject targetMeta = metaLookup.getMeta(targetClass).asDataObject();
						attr = new MetaComputedAttribute(targetMeta, name, type);
						attr.init(metaLookup);
					}
				}
			}
			return attr;
		}
	}

	public void init(JpaQueryFactoryContext context) {
		this.context = context;
	}

	public Object get(MetaComputedAttribute attr) {
		Class<?> clazz = attr.getParent().getImplementationClass();
		Registration registration = map.get(key(clazz, attr.getName()));
		return registration != null ? registration.expressionFactory : null;
	}

	public MetaComputedAttribute get(MetaDataObject meta, String name) {
		Class<?> clazz = meta.getImplementationClass();
		Registration registration = map.get(key(clazz, name));
		return registration != null ? registration.getAttribute() : null;
	}

	public void register(Class<?> targetClass, String name, Object expressionFactory, Type type) {
		Registration registration = new Registration();
		registration.targetClass = targetClass;
		registration.name = name;
		registration.type = type;
		registration.expressionFactory = expressionFactory;
		map.put(key(targetClass, name), registration);
	}

	private String key(Class<?> targetClass, String name) {
		return targetClass.getName() + "." + name;
	}

	@Override
	public Set<String> getForType(Class<?> entityType) {
		Set<String> set = new HashSet<>();
		for (Registration reg : map.values()) {
			MetaComputedAttribute attr = reg.getAttribute();
			MetaDataObject parent = attr.getParent();
			Class<?> parentImpl = parent.getImplementationClass();
			if (parentImpl.isAssignableFrom(entityType)) {
				set.add(attr.getName());
			}
		}
		return set;
	}
}
