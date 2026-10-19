# EzBarrows

A RuneLite plugin that makes Barrows runs easier: it shows which protection prayer to use in each crypt, and draws the shortest route through the tunnels to the chest.

## Features

### Prayer icons on sarcophagi

When you're in a crypt, the brother's sarcophagus shows the protection prayer you need:

| Brother | Prayer |
| --- | --- |
| Ahrim | Protect from Magic |
| Karil | Protect from Missiles |
| Dharok, Guthan, Torag, Verac | Protect from Melee |

The outline around the icon is green when you have the right prayer on and red when you don't. It updates once per game tick, so prayer flicking doesn't make it flash.

Only the sarcophagus in the crypt you're standing in is shown, and the icon disappears once you've killed that brother.

### Tunnel path

In the tunnels, a line on the ground shows the shortest route from where you're standing to the chest. Door locks change every run, so the route only goes through doors you can open.

## Settings

| Setting | Default | Description |
| --- | --- | --- |
| Correct Prayer Colour | Green | Outline colour when you're using the right prayer |
| Wrong Prayer Colour | Red | Outline colour when you're not |
| Hide Killed Brothers | On | Hide the icon once that brother has been killed this run |
| Show Tunnel Path | On | Draw the route to the chest in the tunnels |
| Tunnel Path Colour | Cyan | Colour of the route line |
