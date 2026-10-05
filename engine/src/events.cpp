#include "algerium/events.h"
namespace algerium {
void EventTarget::add_event_listener(const std::string&type,std::function<void(Event&)> cb){listeners[type].push_back(std::move(cb));}
bool EventTarget::dispatch_event(Event& e){auto i=listeners.find(e.type);if(i!=listeners.end())for(auto&cb:i->second)cb(e);return !e.default_prevented;}
}

void algerium::EventTarget::remove_event_listener(const std::string&type){listeners.erase(type);}
