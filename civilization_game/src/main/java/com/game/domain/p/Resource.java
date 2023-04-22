package com.game.domain.p;

import java.util.HashMap;
import java.util.Map;

import com.game.constant.ResourceType;
import com.game.pb.CommonPb;

// 资源 : 自身产出 * (1 + 科技加成)
// 定义成map结构
public class Resource implements Cloneable {
    //1.铁 2.铜 3.石油 4.宝石
    private Map<Integer, Long> resource = new HashMap<Integer, Long>();

    public Resource() {
        init();
    }

    @Override
    public Resource clone() {
        Resource resource = null;
        try {
            resource = (Resource) super.clone();
            HashMap<Integer, Long> map = new HashMap<>();
            this.resource.forEach((key, value) -> {
                map.put(key, value);
            });
            resource.setResource(map);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return resource;
    }

    public void init() {
        for (int i = ResourceType.IRON; i <= ResourceType.STONE; ++i) {
            getResource().put(i, 0L);
        }
    }

    public CommonPb.Resource.Builder wrapPb() {
        CommonPb.Resource.Builder builder = CommonPb.Resource.newBuilder();
        builder.setIron(getResource().get(ResourceType.IRON));
        builder.setCopper(getResource().get(ResourceType.COPPER));
        builder.setOil(getResource().get(ResourceType.OIL));
        builder.setStone(getResource().get(ResourceType.STONE));
        return builder;
    }

    public void unwrapPb(CommonPb.Resource resourcePb) {
        getResource().put(ResourceType.IRON, resourcePb.getIron());
        getResource().put(ResourceType.COPPER, resourcePb.getCopper());
        getResource().put(ResourceType.OIL, resourcePb.getOil());
        getResource().put(ResourceType.STONE, resourcePb.getStone());
    }

    public long getIron() {
        return getResource().get(ResourceType.IRON);
    }

    public long getCopper() {
        return getResource().get(ResourceType.COPPER);
    }


    public long getOil() {
        return getResource().get(ResourceType.OIL);
    }


    public long getStone() {
        return getResource().get(ResourceType.STONE);
    }

    public long getResource(int resourceType) {
        return getResource().get(resourceType);
    }

    public Map<Integer, Long> getResource() {
        return resource;
    }

    public void setResource(Map<Integer, Long> resource) {
        this.resource = resource;
    }

}
