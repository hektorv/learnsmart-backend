# ☁️ Guía de Despliegue: Oracle Cloud "Always Free"

Esta guía te llevará paso a paso para desplegar tu backend en Oracle Cloud completamente gratis, aprovechando su capa "Always Free" de 24GB de RAM.

## 1. Crear la Máquina Virtual (VPS)

1.  Entra en tu consola de Oracle Cloud.
2.  Ve a **Compute** -> **Instances**.
3.  Haz clic en **Create Instance**.
4.  **Name:** Ponle un nombre (ej: `learnsmart-backend`).
5.  **Image and Shape** (¡IMPORTANTE!):
    *   Haz clic en "Edit".
    *   **Image:** Selecciona `Canonical Ubuntu` o `Oracle Linux 8/9`.
    *   **Shape:** Selecciona **Ampere** (ARM).
        *   Asegúrate de seleccionar la serie **VM.Standard.A1.Flex**.
        *   Configura **OCPUs** a `4` y **Memory** a `24 GB`.
    *   *Deberías ver una etiqueta que dice "Always Free Eligible".*
6.  **Networking:** Deja la configuración por defecto (creará una VCN y una Subnet pública).
7.  **Add SSH Keys:**
    *   Selecciona "Generate a key pair for me".
    *   **¡DESCARGA LA PRIVATE KEY!** (`.key` o `.pem`). No podrás recuperarla después.
8.  Haz clic en **Create**.

## 2. Abrir Puertos (Firewall)

Por defecto, Oracle bloquea casi todo. Necesitamos abrir los puertos HTTP/HTTPS y los de tu aplicación.

1.  En la página de detalles de tu instancia, haz clic en el enlace de la **Subnet** (bajo "Primary VNIC").
2.  Haz clic en la **Security List** (ej: `Default Security List for...`).
3.  Haz clic en **Add Ingress Rules**.
4.  Añade una regla para permitir el tráfico:
    *   **Source CIDR:** `0.0.0.0/0` (Desde cualquier lugar).
    *   **Protocol:** TCP.
    *   **Destination Port Range:** `80, 443, 8762` (Gateway), `8080` (Keycloak).
    *   *Nota: Si quieres acceder a los microservicios directamente para debugar, añade también `8081-8085`.*
5.  Haz clic en **Add Ingress Rules**.

## 3. Conectarse al Servidor

Usa tu terminal (o Putty en Windows) con la clave que descargaste:

```bash
# Dale permisos correctos a tu clave (solo lectura para ti)
chmod 400 key-name.key

# Conéctate (el usuario suele ser 'ubuntu' o 'opc' dependiendo de la imagen)
ssh -i key-name.key ubuntu@<TU_IP_PUBLICA>
```

## 4. Desplegar la Aplicación

Una vez dentro del servidor, ejecuta estos comandos:

1.  **Clonar tu código:**
    *(Si tienes el repo privado, necesitarás generar un token de acceso personal en GitHub o subir tu clave SSH pública a GitHub).*
    ```bash
    git clone https://github.com/hektorv/learnsmart-backend.git
    cd learnsmart-backend
    ```

2.  **Preparar el despliegue:**
    ```bash
    # Dar permisos de ejecución al script
    chmod +x deploy.sh
    ```

3.  **Lanzar el despliegue:**
    ```bash
    ./deploy.sh
    ```

    *Este script instalará Docker automáticamente, configurará todo y lanzará los contenedores.*

## 5. Verificar

Espera unos minutos a que todo arranque (la primera vez descarga muchas cosas).

*   **Ver logs en tiempo real:**
    ```bash
    docker compose -f docker-compose.prod.yml logs -f
    ```

*   **Probar acceso:**
    Desde tu navegador, entra a `http://<TU_IP_PUBLICA>:8762/actuator/health` (debería responder el Gateway).

## Solución de Problemas Comunes

*   **Firewall del Sistema Operativo:** A veces, aunque abras los puertos en la consola de Oracle, el firewall de Linux (`iptables` o `ufw`) los bloquea.
    *   Si no conectas, prueba a ejecutar en el servidor:
        ```bash
        sudo iptables -F
        sudo netfilter-persistent save
        ```
        *(Esto borra las reglas del firewall interno para probar. En producción idealmente configurarías reglas específicas).*
