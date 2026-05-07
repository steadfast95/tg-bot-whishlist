package com.gaming.bot.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {
    
    public enum State {
        NONE,
        // Добавление экипировки
        ADDING_EQUIPMENT_NAME,
        ADDING_EQUIPMENT_TYPE,
        ADDING_EQUIPMENT_QUANTITY,
        ADDING_EQUIPMENT_DESCRIPTION,
        // Добавление желания
        ADDING_WISH_NAME,
        ADDING_WISH_TYPE,
        ADDING_WISH_PRIORITY,
        ADDING_WISH_NOTES,
        // Создание персонажа
        CREATING_CHARACTER_NAME,
        CREATING_CHARACTER_CLASS,
        CREATING_CHARACTER_LEVEL,
        CREATING_CHARACTER_DESCRIPTION,
        // Редактирование персонажа
        EDITING_CHARACTER_NAME,
        EDITING_CHARACTER_CLASS,
        EDITING_CHARACTER_LEVEL,
        EDITING_CHARACTER_DESCRIPTION,
        // Создание группы
        CREATING_GROUP_NAME,
        CREATING_GROUP_DESCRIPTION,
        // Добавление участника в группу (устаревшее)
        ADDING_MEMBER_CHARACTER_ID,
        // Присоединение к группе по коду
        JOINING_GROUP_CODE,
        JOINING_GROUP_SELECT_CHARACTER,
        // Редактирование желания
        EDITING_WISH_NAME,
        EDITING_WISH_TYPE,
        EDITING_WISH_PRIORITY,
        EDITING_WISH_NOTES,
        // Импорт базы данных
        WAITING_FOR_IMPORT
    }
    
    private final Map<Long, State> userStates = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> userData = new ConcurrentHashMap<>();
    
    public State getState(Long chatId) {
        return userStates.getOrDefault(chatId, State.NONE);
    }
    
    public void setState(Long chatId, State state) {
        userStates.put(chatId, state);
    }
    
    public void clearState(Long chatId) {
        userStates.remove(chatId);
        userData.remove(chatId);
    }
    
    public void setData(Long chatId, String key, Object value) {
        userData.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>()).put(key, value);
    }
    
    public Object getData(Long chatId, String key) {
        Map<String, Object> data = userData.get(chatId);
        return data != null ? data.get(key) : null;
    }
    
    public Long getDataAsLong(Long chatId, String key) {
        Object value = getData(chatId, key);
        return value != null ? (Long) value : null;
    }
    
    public String getDataAsString(Long chatId, String key) {
        Object value = getData(chatId, key);
        return value != null ? (String) value : null;
    }
    
    public Integer getDataAsInt(Long chatId, String key) {
        Object value = getData(chatId, key);
        return value != null ? (Integer) value : null;
    }
}
