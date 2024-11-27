## Event Service

### Endpoints

События:
- `POST /events` - создание события
- `PATCH /events/{eventId}` - обновление события по `id` (по header убеждаемся, что запрос делает создатель, нельзя обновить `createdDateTime` и `ownerId`)
- `GET /events/{eventId}` - получение события по `id` (если запрашивает создатель, то вернуть с полем `createdDateTime`, иначе без него)
- `GET /events?page={page}&size={size}&ownerId={ownerId}` - получение событий с пагинацией и необязательным фильтров по владельцу
- `DELETE /events/{eventId}` - удаление события по `id` (удалить может только создатель, проверяем по header)

Команды организаторов:
- `POST /events/orgs` - добавление пользователя в команду организаторов
- `PATCH /events/orgs` - обновление данных члена команды организаторов  
- `DELETE /events/{eventId}/orgs/{userId}` - удаление пользователя из команды организаторов
- `GET /events/orgs/{eventId}` - получение всех пользоватей команды организаторов события

### Модели данных

`Event` 
- `id`
- `name`
- `description`
- `createdDateTime`
- `startDateTime`
- `endDateTime`
- `location`
- `ownerId`

`OrgTeamMember`
- `id`
- `eventId`
- `userId`
- `role` (`EXECUTOR`, `MANAGER`)