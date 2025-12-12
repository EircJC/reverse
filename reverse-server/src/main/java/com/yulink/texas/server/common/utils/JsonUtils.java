package com.yulink.texas.server.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yulink.texas.server.common.entity.ListPo;
import com.yulink.texas.server.common.entity.UseSkill;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils {

	private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	private static Gson gsonAll = new GsonBuilder().create();

	public static String toJson(Object src, Type typeOfSrc) {
		return gson.toJson(src, typeOfSrc);
	}
	public static String toJsonAll(Object src, Type typeOfSrc) {
		return gsonAll.toJson(src, typeOfSrc);
	}
	public static <T> T fromJson(String json, Class<T> classOfT) {
		return (T) gson.fromJson(json, classOfT);
	}

	public static <T> String toListJson(List<T> list) {
		ListPo<T> listpo = new ListPo<T>();
		listpo.setList(list);
		return gson.toJson(listpo, new TypeToken<ListPo<T>>() {
		}.getType());
	}

	public static <T> ListPo<T> fromListJson(String json, Type t) {
		return gson.fromJson(json, t);
	}

	public static void main(String[] args) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id","2499");
		jsonObject.put("skillDictionaryNo", "skill3");
		UseSkill useSkill1 = JSONObject.parseObject(jsonObject.toJSONString(), UseSkill.class);
		UseSkill useSkill = JsonUtils.fromJson(jsonObject.toJSONString(), UseSkill.class);
		System.out.println(useSkill.getSkillDictionaryNo());
		System.out.println(useSkill1.getSkillDictionaryNo());
	}
}
