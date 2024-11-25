## Event Service

### Endpoints
- `POST /events` - создание события
- `PATCH /events/{eventId}` - обновление события по `id` (по header убеждаемся, что запрос делает создатель, нельзя обновить `createdDateTime` и `ownerId`)
- `GET /events/{eventId}` - получение события по `id` (если запрашивает создатель, то вернуть с полем `createdDateTime`, иначе без него)
- `GET /events?page={page}&size={size}&ownerId={ownerId}` - получение событий с пагинацией и необязательным фильтров по владельцу
- `DELETE /events/{eventId}` - удаление события по `id` (удалить может только создатель, проверяем по header)
- `POST /events/orgs` - добавить пользователя в группу организаторов


### Models
Модель `Event` включает следующие поля: 
- name
- description
- createdDateTime
- startDateTime
- endDateTime
- location
- ownerId